const API = Object.freeze({
  loginUser: "./user/get/login",
  logout: "./user/logout",
  courses: "./course/list/page",
  files: "./course/file/list/page",
  download: "./course/file/download",
  ask: "./learning/knowledge/ask",
  generateQuestions: "./learning/question/generate",
  answerQuestion: "./learning/question/answer",
  wrongQuestions: "./learning/question/wrong/list/page",
  profile: "./learning/profile/get",
  analyzeProfile: "./learning/profile/analyze",
});

const state = {
  user: null,
  courses: [],
  selectedCourse: null,
  conversationId: getConversationId(),
  questions: [],
};

const elements = {
  globalAlert: document.querySelector("#global-alert"),
  globalAlertText: document.querySelector("#global-alert-text"),
  courseList: document.querySelector("#course-list"),
  currentCourseName: document.querySelector("#current-course-name"),
  currentCourseDescription: document.querySelector("#current-course-description"),
  resourceList: document.querySelector("#resource-list"),
  resourceContext: document.querySelector("#resource-context"),
  questionList: document.querySelector("#question-list"),
  wrongList: document.querySelector("#wrong-list"),
  profileContent: document.querySelector("#profile-content"),
  assistantCourse: document.querySelector("#assistant-course strong"),
  chatList: document.querySelector("#chat-list"),
  askInput: document.querySelector("#ask-input"),
  askSubmit: document.querySelector("#ask-submit"),
  generateButton: document.querySelector("#generate-questions-button"),
  heroAskButton: document.querySelector("#hero-ask-button"),
  heroPracticeButton: document.querySelector("#hero-practice-button"),
};

class ApiError extends Error {
  constructor(message, code, status) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

function apiUrl(path) {
  return new URL(path, document.baseURI).toString();
}

function initializeIcons() {
  if (window.lucide) {
    window.lucide.createIcons();
  }
}

function getConversationId() {
  const storageKey = "studyAi.studentConversationId";
  const saved = sessionStorage.getItem(storageKey);
  if (saved) {
    return saved;
  }

  const generated = window.crypto?.randomUUID?.() || `student-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  sessionStorage.setItem(storageKey, generated);
  return generated;
}

async function request(path, options = {}) {
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...options.headers,
    },
    ...options,
  });

  let payload;
  try {
    payload = await response.json();
  } catch {
    throw new ApiError("服务器返回了无法识别的响应", null, response.status);
  }

  if (!response.ok || payload.code !== 0) {
    if (payload.code === 40100 || response.status === 401) {
      redirectToLogin();
    }
    throw new ApiError(payload.message || "请求失败", payload.code, response.status);
  }

  return payload.data;
}

function redirectToLogin() {
  const redirect = `${window.location.pathname}${window.location.search}`;
  window.location.replace(`./login.html?redirect=${encodeURIComponent(redirect)}`);
}

function showAlert(message) {
  elements.globalAlertText.textContent = message;
  elements.globalAlert.hidden = false;
}

function hideAlert() {
  elements.globalAlert.hidden = true;
  elements.globalAlertText.textContent = "";
}

function errorMessage(error, fallback) {
  if (error instanceof TypeError) {
    return "无法连接服务器，请确认后端服务已启动";
  }
  return error?.message || fallback;
}

function createEmptyState(icon, title, description) {
  const wrapper = document.createElement("div");
  wrapper.className = "empty-state";
  const iconElement = document.createElement("i");
  iconElement.dataset.lucide = icon;
  iconElement.setAttribute("aria-hidden", "true");
  const strong = document.createElement("strong");
  strong.textContent = title;
  const paragraph = document.createElement("p");
  paragraph.textContent = description;
  wrapper.append(iconElement, strong, paragraph);
  return wrapper;
}

function createLoadingState(label) {
  const wrapper = document.createElement("div");
  wrapper.className = "loading-state";
  const spinner = document.createElement("span");
  spinner.className = "spinner";
  wrapper.append(spinner, document.createTextNode(label));
  return wrapper;
}

function replaceContent(container, content) {
  container.replaceChildren(content);
  initializeIcons();
}

function setButtonLoading(button, loading, loadingLabel = "处理中") {
  if (!button) return;
  if (loading) {
    button.dataset.originalHtml = button.innerHTML;
    button.disabled = true;
    button.textContent = loadingLabel;
  } else {
    button.disabled = false;
    if (button.dataset.originalHtml) {
      button.innerHTML = button.dataset.originalHtml;
      delete button.dataset.originalHtml;
      initializeIcons();
    }
  }
}

function formatDate(value) {
  if (!value) return "未知时间";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "未知时间";
  return new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" }).format(date);
}

function formatFileSize(bytes) {
  const size = Number(bytes);
  if (!Number.isFinite(size) || size < 0) return "未知大小";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function roleLabel(role) {
  return { student: "学生", teacher: "教师", admin: "管理员" }[role] || "用户";
}

function renderUser(user) {
  document.querySelector("#user-name").textContent = user.username || "学生";
  document.querySelector("#user-chip small").textContent = roleLabel(user.role);
  const avatar = document.querySelector("#user-avatar");
  if (user.avatar) {
    const image = document.createElement("img");
    image.src = user.avatar;
    image.alt = `${user.username || "用户"}的头像`;
    avatar.replaceChildren(image);
  }
}

async function loadLoginUser() {
  const user = await request(API.loginUser);
  if (user.role !== "student") {
    throw new ApiError("当前账号不是学生账号，无法进入学生工作台", 40101, 403);
  }
  state.user = user;
  sessionStorage.setItem("studyAi.loginUser", JSON.stringify(user));
  renderUser(user);
  return user;
}

async function loadCourses(courseName = "") {
  replaceContent(elements.courseList, createLoadingState("正在加载课程"));
  const page = await request(API.courses, {
    method: "POST",
    body: JSON.stringify({ current: 1, pageSize: 30, course_name: courseName || undefined }),
  });
  state.courses = Array.isArray(page?.records) ? page.records : [];
  document.querySelector("#course-count").textContent = page?.total ?? state.courses.length;
  renderCourses();

  if (state.courses.length && !state.selectedCourse) {
    await selectCourse(state.courses[0]);
  } else if (!state.courses.length) {
    clearSelectedCourse();
  }
}

function renderCourses() {
  elements.courseList.replaceChildren();
  if (!state.courses.length) {
    replaceContent(elements.courseList, createEmptyState("book-open", "没有找到课程", "尝试更换搜索词，或等待教师发布课程。"));
    return;
  }

  state.courses.forEach((course) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "course-card";
    if (state.selectedCourse?.course_id === course.course_id) {
      button.classList.add("is-selected");
    }

    const copy = document.createElement("span");
    const title = document.createElement("h3");
    title.textContent = course.course_name || "未命名课程";
    const description = document.createElement("p");
    description.textContent = course.description || "暂无课程简介";
    copy.append(title, description);

    const tag = document.createElement("span");
    tag.className = "tag";
    tag.textContent = formatDate(course.update_time || course.create_time);
    button.append(copy, tag);
    button.addEventListener("click", () => selectCourse(course));
    elements.courseList.append(button);
  });
}

function clearSelectedCourse() {
  state.selectedCourse = null;
  elements.currentCourseName.textContent = "请选择一门课程";
  elements.currentCourseDescription.textContent = "课程加载完成后，从课程列表选择课程开始学习。";
  elements.resourceContext.textContent = "请先选择课程";
  elements.assistantCourse.textContent = "尚未选择课程";
  setCourseActionsEnabled(false);
  document.querySelector("#resource-count").textContent = "—";
  document.querySelector("#wrong-count").textContent = "—";
}

async function selectCourse(course) {
  state.selectedCourse = course;
  elements.currentCourseName.textContent = course.course_name || "未命名课程";
  elements.currentCourseDescription.textContent = course.description || "暂无课程简介，可从资料与 AI 问答开始学习。";
  elements.resourceContext.textContent = course.course_name || "当前课程";
  elements.assistantCourse.textContent = course.course_name || "当前课程";
  setCourseActionsEnabled(true);
  renderCourses();
  addChatMessage("ai", `已切换到“${course.course_name || "当前课程"}”。你可以针对课程内容提问。`);
  await Promise.all([loadCourseFiles(course.course_id), loadWrongQuestions(course.course_id)]);
}

function setCourseActionsEnabled(enabled) {
  elements.askInput.disabled = !enabled;
  elements.askSubmit.disabled = !enabled;
  elements.generateButton.disabled = !enabled;
  elements.heroAskButton.disabled = !enabled;
  elements.heroPracticeButton.disabled = !enabled;
}

async function loadCourseFiles(courseId) {
  replaceContent(elements.resourceList, createLoadingState("正在加载课程资料"));
  try {
    const page = await request(API.files, {
      method: "POST",
      body: JSON.stringify({ current: 1, pageSize: 30, courseId }),
    });
    const files = Array.isArray(page?.records) ? page.records : [];
    document.querySelector("#resource-count").textContent = page?.total ?? files.length;
    renderFiles(files);
  } catch (error) {
    document.querySelector("#resource-count").textContent = "—";
    replaceContent(elements.resourceList, createEmptyState("circle-alert", "资料加载失败", errorMessage(error, "无法读取课程资料")));
  }
}

function renderFiles(files) {
  elements.resourceList.replaceChildren();
  if (!files.length) {
    replaceContent(elements.resourceList, createEmptyState("folder-open", "暂无课程资料", "教师还没有为这门课程发布资料。"));
    return;
  }

  files.forEach((file) => {
    const card = document.createElement("article");
    card.className = "resource-card";

    const icon = document.createElement("span");
    icon.className = "file-icon";
    const iconElement = document.createElement("i");
    iconElement.dataset.lucide = "file-text";
    iconElement.setAttribute("aria-hidden", "true");
    icon.append(iconElement);

    const copy = document.createElement("div");
    const title = document.createElement("h3");
    title.textContent = file.file_name || "未命名资料";
    const meta = document.createElement("p");
    meta.textContent = `${file.file_type || "文件"} · ${formatFileSize(file.file_size)} · ${formatDate(file.update_time || file.create_time)}`;
    copy.append(title, meta);

    const download = document.createElement("a");
    download.className = "download-button";
    download.href = `${apiUrl(API.download)}?id=${encodeURIComponent(file.id)}`;
    download.setAttribute("aria-label", `下载${file.file_name || "课程资料"}`);
    download.title = "下载资料";
    const downloadIcon = document.createElement("i");
    downloadIcon.dataset.lucide = "download";
    downloadIcon.setAttribute("aria-hidden", "true");
    download.append(downloadIcon);

    card.append(icon, copy, download);
    elements.resourceList.append(card);
  });
  initializeIcons();
}

async function loadWrongQuestions(courseId) {
  replaceContent(elements.wrongList, createLoadingState("正在读取错题回流"));
  try {
    const page = await request(API.wrongQuestions, {
      method: "POST",
      body: JSON.stringify({ current: 1, pageSize: 10, courseId }),
    });
    const records = Array.isArray(page?.records) ? page.records : [];
    document.querySelector("#wrong-count").textContent = page?.total ?? records.length;
    renderWrongQuestions(records);
  } catch (error) {
    document.querySelector("#wrong-count").textContent = "—";
    replaceContent(elements.wrongList, createEmptyState("circle-alert", "错题加载失败", errorMessage(error, "无法读取错题记录")));
  }
}

function renderWrongQuestions(records) {
  elements.wrongList.replaceChildren();
  if (!records.length) {
    replaceContent(elements.wrongList, createEmptyState("badge-check", "当前课程暂无错题", "完成练习后，错误答案与 AI 反馈会回流到这里。"));
    return;
  }

  records.forEach((record) => {
    const card = document.createElement("article");
    card.className = "wrong-card";
    const meta = document.createElement("div");
    meta.className = "wrong-meta";
    meta.append(createTag(record.questionType || "题目"), createTag(record.difficulty || "难度未知", true), createTag(formatDate(record.createTime)));
    const title = document.createElement("h3");
    title.textContent = record.question || "题目内容为空";
    const answerGrid = document.createElement("div");
    answerGrid.className = "wrong-answer-grid";
    answerGrid.append(createAnswerBlock("你的答案", record.userAnswer || "未填写"), createAnswerBlock("正确答案", record.correctAnswer || "暂无"));
    card.append(meta, title, answerGrid);
    if (record.aiFeedback || record.analysis) {
      const feedback = document.createElement("p");
      feedback.textContent = record.aiFeedback || record.analysis;
      card.append(feedback);
    }
    elements.wrongList.append(card);
  });
}

function createTag(text, warm = false) {
  const tag = document.createElement("span");
  tag.className = warm ? "tag warm" : "tag";
  tag.textContent = text;
  return tag;
}

function createAnswerBlock(label, value) {
  const block = document.createElement("div");
  block.className = "answer-block";
  const span = document.createElement("span");
  span.textContent = label;
  const strong = document.createElement("strong");
  strong.textContent = value;
  block.append(span, strong);
  return block;
}

async function loadProfile() {
  replaceContent(elements.profileContent, createLoadingState("正在读取学习画像"));
  try {
    const profile = await request(`${API.profile}?userId=${encodeURIComponent(state.user.id)}`);
    renderProfile(profile);
  } catch (error) {
    document.querySelector("#knowledge-level").textContent = "—";
    replaceContent(elements.profileContent, createEmptyState("scan-search", "暂无学习画像", "完成问答与练习后，可点击重新分析生成画像。"));
  }
}

function renderProfile(profile) {
  if (!profile) {
    replaceContent(elements.profileContent, createEmptyState("scan-search", "暂无学习画像", "完成学习活动后生成画像。"));
    return;
  }

  document.querySelector("#knowledge-level").textContent = profile.knowledgeLevel ?? "—";
  const wrapper = document.createElement("div");
  const grid = document.createElement("div");
  grid.className = "profile-grid";
  grid.append(
    createProfileItem("学习风格", profile.learningStyle || "待分析"),
    createProfileItem("学习速度", profile.learningSpeed || "待分析"),
    createProfileItem("兴趣方向", profile.interest || "待分析"),
    createProfileItem("错误偏好", profile.errorPreference || "待分析"),
  );
  wrapper.append(grid);

  const weaknessList = document.createElement("div");
  weaknessList.className = "weakness-list";
  const weaknesses = Array.isArray(profile.weakness) ? profile.weakness : [];
  if (weaknesses.length) {
    weaknesses.forEach((weakness) => weaknessList.append(createTag(weakness, true)));
  } else {
    weaknessList.append(createTag("暂未识别薄弱点"));
  }
  wrapper.append(weaknessList);
  replaceContent(elements.profileContent, wrapper);
}

function createProfileItem(label, value) {
  const item = document.createElement("div");
  item.className = "profile-item";
  const span = document.createElement("span");
  span.textContent = label;
  const strong = document.createElement("strong");
  strong.textContent = value;
  item.append(span, strong);
  return item;
}

async function analyzeProfile() {
  const button = document.querySelector("#analyze-profile-button");
  setButtonLoading(button, true, "分析中");
  hideAlert();
  try {
    const profile = await request(API.analyzeProfile, {
      method: "POST",
      body: JSON.stringify({ userId: state.user.id }),
    });
    renderProfile(profile);
  } catch (error) {
    showAlert(errorMessage(error, "学习画像分析失败"));
  } finally {
    setButtonLoading(button, false);
  }
}

function addChatMessage(role, text) {
  const message = document.createElement("div");
  message.className = `chat-message ${role === "user" ? "user-message" : "ai-message"}`;
  message.textContent = text;
  elements.chatList.append(message);
  elements.chatList.scrollTop = elements.chatList.scrollHeight;
}

async function askKnowledge(event) {
  event.preventDefault();
  const question = elements.askInput.value.trim();
  if (!state.selectedCourse || !question) return;

  addChatMessage("user", question);
  elements.askInput.value = "";
  setButtonLoading(elements.askSubmit, true, "回答中");

  try {
    const result = await request(API.ask, {
      method: "POST",
      body: JSON.stringify({
        courseId: state.selectedCourse.course_id,
        question,
        conversationId: state.conversationId,
      }),
    });
    if (result?.conversationId) {
      state.conversationId = result.conversationId;
      sessionStorage.setItem("studyAi.studentConversationId", result.conversationId);
    }
    addChatMessage("ai", result?.answer || "AI 暂未返回回答。");
  } catch (error) {
    addChatMessage("ai", `回答失败：${errorMessage(error, "请稍后重试")}`);
  } finally {
    setButtonLoading(elements.askSubmit, false);
  }
}

async function generateQuestions() {
  if (!state.selectedCourse) return;
  const count = Number(document.querySelector("#question-count").value) || 3;
  setButtonLoading(elements.generateButton, true, "生成中");
  replaceContent(elements.questionList, createLoadingState("AI 正在生成练习"));
  hideAlert();
  try {
    const questions = await request(API.generateQuestions, {
      method: "POST",
      body: JSON.stringify({
        courseId: state.selectedCourse.course_id,
        questionCount: count,
        conversationId: state.conversationId,
      }),
    });
    state.questions = Array.isArray(questions) ? questions : [];
    renderQuestions();
  } catch (error) {
    replaceContent(elements.questionList, createEmptyState("circle-alert", "练习生成失败", errorMessage(error, "请稍后重试")));
  } finally {
    setButtonLoading(elements.generateButton, false);
  }
}

function renderQuestions() {
  elements.questionList.replaceChildren();
  if (!state.questions.length) {
    replaceContent(elements.questionList, createEmptyState("notebook-pen", "未生成题目", "请重新尝试生成练习。"));
    return;
  }

  state.questions.forEach((question, index) => {
    const card = document.createElement("article");
    card.className = "question-card";
    const meta = document.createElement("div");
    meta.className = "question-meta";
    meta.append(createTag(`第 ${index + 1} 题`), createTag(question.questionType || "题目"), createTag(question.difficulty || "难度未知", true));
    const title = document.createElement("h3");
    title.textContent = question.question || "题目内容为空";
    const form = document.createElement("form");
    form.className = "answer-form";
    const input = document.createElement("input");
    input.type = "text";
    input.placeholder = "输入你的答案";
    input.setAttribute("aria-label", `第 ${index + 1} 题答案`);
    input.required = true;
    const button = document.createElement("button");
    button.type = "submit";
    button.className = "primary-button compact-button";
    button.textContent = "提交答案";
    form.append(input, button);
    form.addEventListener("submit", (event) => submitAnswer(event, question, input, button, card));
    card.append(meta, title, form);
    elements.questionList.append(card);
  });
}

async function submitAnswer(event, question, input, button, card) {
  event.preventDefault();
  const answer = input.value.trim();
  if (!answer) return;
  setButtonLoading(button, true, "批改中");
  try {
    const result = await request(API.answerQuestion, {
      method: "POST",
      body: JSON.stringify({ questionId: question.id, userAnswer: answer, conversationId: state.conversationId }),
    });
    input.disabled = true;
    const feedback = document.createElement("div");
    feedback.className = result?.correct ? "answer-feedback" : "answer-feedback is-wrong";
    const status = result?.correct ? "回答正确" : `回答错误，正确答案：${result?.correctAnswer || "暂无"}`;
    feedback.textContent = `${status}${result?.aiFeedback ? `。${result.aiFeedback}` : result?.analysis ? `。${result.analysis}` : ""}`;
    card.append(feedback);
    if (!result?.correct && state.selectedCourse) {
      await loadWrongQuestions(state.selectedCourse.course_id);
    }
  } catch (error) {
    showAlert(errorMessage(error, "答案提交失败"));
  } finally {
    setButtonLoading(button, false);
    if (input.disabled) button.disabled = true;
  }
}

async function logout() {
  const button = document.querySelector("#logout-button");
  button.disabled = true;
  try {
    await request(API.logout, { method: "POST" });
  } catch (error) {
    if (error?.code !== 40100) {
      showAlert(errorMessage(error, "退出登录失败"));
      button.disabled = false;
      return;
    }
  }
  sessionStorage.removeItem("studyAi.loginUser");
  window.location.replace("./login.html");
}

async function initialize() {
  initializeIcons();
  document.querySelector("#alert-close").addEventListener("click", hideAlert);
  document.querySelector("#logout-button").addEventListener("click", logout);
  document.querySelector("#refresh-courses").addEventListener("click", () => loadCourses(document.querySelector("#course-search-input").value.trim()).catch((error) => showAlert(errorMessage(error, "课程加载失败"))));
  document.querySelector("#course-search-form").addEventListener("submit", (event) => {
    event.preventDefault();
    state.selectedCourse = null;
    loadCourses(document.querySelector("#course-search-input").value.trim()).catch((error) => showAlert(errorMessage(error, "课程搜索失败")));
  });
  document.querySelector("#ask-form").addEventListener("submit", askKnowledge);
  document.querySelector("#generate-questions-button").addEventListener("click", generateQuestions);
  document.querySelector("#hero-practice-button").addEventListener("click", () => {
    document.querySelector("#practice").scrollIntoView({ behavior: "smooth" });
    generateQuestions();
  });
  document.querySelector("#hero-ask-button").addEventListener("click", () => elements.askInput.focus());
  document.querySelector("#analyze-profile-button").addEventListener("click", analyzeProfile);

  try {
    await loadLoginUser();
    await Promise.all([loadCourses(), loadProfile()]);
  } catch (error) {
    showAlert(errorMessage(error, "学生工作台加载失败"));
    replaceContent(elements.courseList, createEmptyState("circle-alert", "无法加载课程", errorMessage(error, "请稍后重试")));
    replaceContent(elements.profileContent, createEmptyState("circle-alert", "无法加载画像", errorMessage(error, "请稍后重试")));
  }
}

document.addEventListener("DOMContentLoaded", initialize);
