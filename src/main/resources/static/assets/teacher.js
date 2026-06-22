const API = Object.freeze({
  loginUser: "./user/get/login",
  logout: "./user/logout",
  getProfile: "./user/profile/get",
  updateProfile: "./user/profile/update",
  listCourses: "./course/list/page",
  addCourse: "./course/add",
  updateCourse: "./course/update",
  deleteCourse: "./course/delete",
  listFiles: "./course/file/list/page",
  uploadFile: "./course/file/upload",
  updateFile: "./course/file/update",
  deleteFile: "./course/file/delete",
  downloadFile: "./course/file/download",
});

const state = {
  user: null,
  profile: null,
  courses: [],
  selectedCourse: null,
  files: [],
  fileCurrent: 1,
  filePageSize: 8,
  fileTotal: 0,
  filePages: 1,
  fileSearch: "",
  pendingFile: null,
  pendingDelete: null,
  toastTimer: null,
};

const elements = {
  alert: document.querySelector("#global-alert"),
  alertText: document.querySelector("#global-alert-text"),
  courseList: document.querySelector("#course-list"),
  emptyCourse: document.querySelector("#empty-course"),
  courseWorkspace: document.querySelector("#course-workspace"),
  fileList: document.querySelector("#file-list"),
  uploadZone: document.querySelector("#upload-zone"),
  fileInput: document.querySelector("#course-file-input"),
  uploadButton: document.querySelector("#upload-button"),
  courseDialog: document.querySelector("#course-dialog"),
  courseForm: document.querySelector("#course-form"),
  renameDialog: document.querySelector("#rename-dialog"),
  renameForm: document.querySelector("#rename-form"),
  profileDialog: document.querySelector("#profile-dialog"),
  profileForm: document.querySelector("#profile-form"),
  deleteDialog: document.querySelector("#delete-dialog"),
  deleteForm: document.querySelector("#delete-form"),
  toast: document.querySelector("#toast"),
  toastText: document.querySelector("#toast-text"),
};

class ApiError extends Error {
  constructor(message, code, status) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

function initializeIcons() {
  if (window.lucide) window.lucide.createIcons();
}

function apiUrl(path) {
  return new URL(path, document.baseURI).toString();
}

async function request(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
      ...(!isFormData && options.body ? { "Content-Type": "application/json" } : {}),
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
    if (payload.code === 40100 || response.status === 401) redirectToLogin();
    throw new ApiError(payload.message || "请求失败", payload.code, response.status);
  }
  return payload.data;
}

function redirectToLogin() {
  const redirect = `${window.location.pathname}${window.location.search}`;
  window.location.replace(`./login.html?redirect=${encodeURIComponent(redirect)}`);
}

function errorMessage(error, fallback) {
  if (error instanceof TypeError) return "无法连接服务器，请确认后端服务已启动";
  return error?.message || fallback;
}

function showAlert(message) {
  elements.alertText.textContent = message;
  elements.alert.hidden = false;
}

function hideAlert() {
  elements.alert.hidden = true;
  elements.alertText.textContent = "";
}

function showToast(message) {
  window.clearTimeout(state.toastTimer);
  elements.toastText.textContent = message;
  elements.toast.hidden = false;
  state.toastTimer = window.setTimeout(() => { elements.toast.hidden = true; }, 2600);
}

function setButtonLoading(button, loading, label = "处理中") {
  if (loading) {
    button.dataset.html = button.innerHTML;
    button.disabled = true;
    button.textContent = label;
  } else {
    button.disabled = false;
    if (button.dataset.html) {
      button.innerHTML = button.dataset.html;
      delete button.dataset.html;
      initializeIcons();
    }
  }
}

function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";
  return new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" }).format(date);
}

function formatFileSize(value) {
  const bytes = Number(value);
  if (!Number.isFinite(bytes) || bytes < 0) return "未知大小";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function avatarContent(container, user) {
  container.replaceChildren();
  if (user?.avatar) {
    const image = document.createElement("img");
    image.src = user.avatar;
    image.alt = `${user.username || "教师"}的头像`;
    container.append(image);
  } else {
    const initial = document.createElement("span");
    initial.textContent = (user?.username || "T").slice(0, 1).toUpperCase();
    container.append(initial);
  }
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

async function loadLoginUser() {
  const user = await request(API.loginUser);
  if (user.role !== "teacher") throw new ApiError("当前账号没有教师权限", 40101, 403);
  state.user = user;
  sessionStorage.setItem("studyAi.loginUser", JSON.stringify(user));
  document.querySelector("#user-name").textContent = user.username || "教师";
  avatarContent(document.querySelector("#user-avatar"), user);
  return user;
}

async function loadProfile() {
  const profile = await request(API.getProfile);
  state.profile = profile;
  const teacherName = profile?.teacher?.teacher_name;
  if (teacherName) document.querySelector("#user-name").textContent = teacherName;
  return profile;
}

async function fetchCoursePage(courseName = "") {
  return request(API.listCourses, {
    method: "POST",
    body: JSON.stringify({ current: 1, pageSize: 100, teacher_id: state.user.id, course_name: courseName || undefined }),
  });
}

async function loadCourses(courseName = "") {
  elements.courseList.replaceChildren(createLoadingState("正在加载课程"));
  initializeIcons();
  const page = await fetchCoursePage(courseName);
  state.courses = Array.isArray(page?.records) ? page.records : [];
  document.querySelector("#metric-courses").textContent = page?.total ?? state.courses.length;
  renderCourses();

  if (state.selectedCourse) {
    const updated = state.courses.find((course) => course.course_id === state.selectedCourse.course_id);
    if (updated) await selectCourse(updated);
    else clearSelectedCourse();
  } else if (state.courses.length) {
    await selectCourse(state.courses[0]);
  } else {
    clearSelectedCourse();
  }
}

function renderCourses() {
  elements.courseList.replaceChildren();
  if (!state.courses.length) {
    elements.courseList.append(createEmptyState("book-plus", "暂无课程", "点击上方加号创建第一门课程。"));
    initializeIcons();
    return;
  }

  state.courses.forEach((course) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "course-item";
    if (course.course_id === state.selectedCourse?.course_id) button.classList.add("is-selected");
    const copy = document.createElement("span");
    const title = document.createElement("strong");
    title.textContent = course.course_name || "未命名课程";
    const description = document.createElement("small");
    description.textContent = course.description || "暂无课程介绍";
    copy.append(title, description);
    const date = document.createElement("span");
    date.textContent = formatDate(course.update_time || course.create_time);
    button.append(copy, date);
    button.addEventListener("click", () => selectCourse(course));
    elements.courseList.append(button);
  });
}

function clearSelectedCourse() {
  state.selectedCourse = null;
  state.files = [];
  elements.emptyCourse.hidden = false;
  elements.courseWorkspace.hidden = true;
  document.querySelector("#metric-selected-files").textContent = "—";
  document.querySelector("#metric-selected-label").textContent = "尚未选择课程";
  document.querySelector("#metric-updated").textContent = "—";
  updateUploadAvailability();
  renderCourses();
}

async function selectCourse(course) {
  state.selectedCourse = course;
  state.fileCurrent = 1;
  state.fileSearch = "";
  document.querySelector("#file-search").value = "";
  elements.emptyCourse.hidden = true;
  elements.courseWorkspace.hidden = false;
  document.querySelector("#current-course-name").textContent = course.course_name || "未命名课程";
  document.querySelector("#current-course-description").textContent = course.description || "暂无课程介绍。";
  document.querySelector("#metric-selected-label").textContent = course.course_name || "当前课程";
  document.querySelector("#metric-updated").textContent = formatDate(course.update_time || course.create_time);
  renderCourses();
  updateUploadAvailability();
  await loadFiles();
}

async function loadAllFileCount() {
  const page = await request(API.listFiles, {
    method: "POST",
    body: JSON.stringify({ current: 1, pageSize: 1, teacherId: state.user.id }),
  });
  document.querySelector("#metric-files").textContent = page?.total ?? 0;
}

async function loadFiles() {
  if (!state.selectedCourse) return;
  elements.fileList.replaceChildren(createLoadingState("正在加载课程资料"));
  initializeIcons();
  try {
    const page = await request(API.listFiles, {
      method: "POST",
      body: JSON.stringify({ current: state.fileCurrent, pageSize: state.filePageSize, courseId: state.selectedCourse.course_id, teacherId: state.user.id, fileName: state.fileSearch || undefined }),
    });
    state.files = Array.isArray(page?.records) ? page.records : [];
    state.fileTotal = Number(page?.total) || 0;
    state.filePages = Math.max(1, Number(page?.pages) || Math.ceil(state.fileTotal / state.filePageSize) || 1);
    if (state.fileCurrent > state.filePages) {
      state.fileCurrent = state.filePages;
      return loadFiles();
    }
    document.querySelector("#metric-selected-files").textContent = state.fileTotal;
    renderFiles();
    updateFilePagination();
  } catch (error) {
    elements.fileList.replaceChildren(createEmptyState("circle-alert", "资料加载失败", errorMessage(error, "无法读取课程资料")));
    initializeIcons();
    showAlert(errorMessage(error, "资料加载失败"));
  }
}

function renderFiles() {
  elements.fileList.replaceChildren();
  if (!state.files.length) {
    elements.fileList.append(createEmptyState("folder-open", "暂无课程资料", state.fileSearch ? "没有找到匹配的资料。" : "从上方上传区添加第一份课程资料。"));
    initializeIcons();
    return;
  }

  state.files.forEach((file) => {
    const row = document.createElement("article");
    row.className = "file-row";
    const icon = document.createElement("span");
    icon.className = "file-icon";
    const iconElement = document.createElement("i");
    iconElement.dataset.lucide = "file-text";
    iconElement.setAttribute("aria-hidden", "true");
    icon.append(iconElement);

    const copy = document.createElement("div");
    copy.className = "file-copy";
    const text = document.createElement("div");
    const title = document.createElement("strong");
    title.textContent = file.file_name || "未命名资料";
    const meta = document.createElement("small");
    meta.textContent = `${formatFileSize(file.file_size)} · 更新于 ${formatDate(file.update_time || file.create_time)}`;
    text.append(title, meta);
    const type = document.createElement("span");
    type.className = "file-type";
    type.textContent = file.file_type || "FILE";
    copy.append(text, type);

    const actions = document.createElement("div");
    actions.className = "file-actions";
    const download = document.createElement("a");
    download.className = "icon-button";
    download.href = `${apiUrl(API.downloadFile)}?id=${encodeURIComponent(file.id)}`;
    download.setAttribute("aria-label", `下载${file.file_name || "资料"}`);
    download.title = "下载资料";
    const downloadIcon = document.createElement("i");
    downloadIcon.dataset.lucide = "download";
    downloadIcon.setAttribute("aria-hidden", "true");
    download.append(downloadIcon);
    actions.append(download, createActionButton("pencil", "重命名资料", () => openRenameDialog(file)), createActionButton("trash-2", "删除资料", () => openDeleteDialog("file", file)));
    row.append(icon, copy, actions);
    elements.fileList.append(row);
  });
  initializeIcons();
}

function createActionButton(icon, label, handler) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "icon-button";
  button.setAttribute("aria-label", label);
  button.title = label;
  const iconElement = document.createElement("i");
  iconElement.dataset.lucide = icon;
  iconElement.setAttribute("aria-hidden", "true");
  button.append(iconElement);
  button.addEventListener("click", handler);
  return button;
}

function updateFilePagination() {
  document.querySelector("#file-page-summary").textContent = `共 ${state.fileTotal} 份资料`;
  document.querySelector("#file-page-indicator").textContent = `${state.fileCurrent} / ${state.filePages}`;
  document.querySelector("#previous-file-page").disabled = state.fileCurrent <= 1;
  document.querySelector("#next-file-page").disabled = state.fileCurrent >= state.filePages;
}

function openCourseDialog(course = null) {
  elements.courseForm.reset();
  document.querySelector("#course-form-error").textContent = "";
  document.querySelector("#course-id").value = course?.course_id ?? "";
  document.querySelector("#course-name-input").value = course?.course_name || "";
  document.querySelector("#course-description-input").value = course?.description || "";
  document.querySelector("#course-dialog-title").textContent = course ? "编辑课程" : "新增课程";
  document.querySelector("#course-dialog-code").textContent = course ? "EDIT COURSE" : "NEW COURSE";
  elements.courseDialog.showModal();
  document.querySelector("#course-name-input").focus();
}

async function saveCourse(event) {
  event.preventDefault();
  const id = document.querySelector("#course-id").value;
  const name = document.querySelector("#course-name-input").value.trim();
  const description = document.querySelector("#course-description-input").value.trim();
  const error = document.querySelector("#course-form-error");
  if (!name) {
    error.textContent = "请输入课程名称";
    return;
  }
  const button = document.querySelector("#save-course-button");
  setButtonLoading(button, true, "保存中");
  error.textContent = "";
  try {
    if (id) {
      await request(API.updateCourse, { method: "POST", body: JSON.stringify({ course_id: Number(id), course_name: name, description }) });
    } else {
      await request(API.addCourse, { method: "POST", body: JSON.stringify({ course_name: name, description }) });
    }
    elements.courseDialog.close();
    showToast(id ? "课程已更新" : "课程已创建");
    await loadCourses(document.querySelector("#course-search").value.trim());
  } catch (requestError) {
    error.textContent = errorMessage(requestError, "课程保存失败");
  } finally {
    setButtonLoading(button, false);
  }
}

function setPendingFile(file) {
  state.pendingFile = file || null;
  document.querySelector("#selected-file-name").textContent = file ? `${file.name} · ${formatFileSize(file.size)}` : "PDF、Word、PPT、Excel、TXT、Markdown";
  document.querySelector("#upload-status").textContent = "";
  updateUploadAvailability();
}

function updateUploadAvailability() {
  elements.uploadButton.disabled = !state.selectedCourse || !state.pendingFile;
}

async function uploadFile() {
  if (!state.selectedCourse || !state.pendingFile) return;
  const button = elements.uploadButton;
  const status = document.querySelector("#upload-status");
  const formData = new FormData();
  formData.append("file", state.pendingFile);
  const analyze = document.querySelector("#chapter-analysis").checked;
  setButtonLoading(button, true, "AI 校验中");
  status.textContent = analyze ? "正在转换内容并解析章节，请保持页面开启。" : "正在转换内容并执行资料校验。";
  hideAlert();
  try {
    await request(`${API.uploadFile}?courseId=${encodeURIComponent(state.selectedCourse.course_id)}&needChapterAnalysis=${analyze}`, { method: "POST", body: formData });
    setPendingFile(null);
    elements.fileInput.value = "";
    status.textContent = "上传完成，资料已进入课程知识库。";
    showToast("资料已上传");
    await Promise.all([loadFiles(), loadAllFileCount()]);
  } catch (error) {
    status.textContent = errorMessage(error, "资料上传失败");
    showAlert(status.textContent);
  } finally {
    setButtonLoading(button, false);
    updateUploadAvailability();
  }
}

function openRenameDialog(file) {
  document.querySelector("#rename-file-id").value = file.id;
  document.querySelector("#rename-file-name").value = file.file_name || "";
  document.querySelector("#rename-form-error").textContent = "";
  elements.renameDialog.showModal();
  document.querySelector("#rename-file-name").focus();
}

async function renameFile(event) {
  event.preventDefault();
  const id = Number(document.querySelector("#rename-file-id").value);
  const fileName = document.querySelector("#rename-file-name").value.trim();
  const error = document.querySelector("#rename-form-error");
  if (!fileName) {
    error.textContent = "请输入资料名称";
    return;
  }
  const button = document.querySelector("#save-file-name-button");
  setButtonLoading(button, true, "保存中");
  try {
    await request(API.updateFile, { method: "POST", body: JSON.stringify({ id, file_name: fileName }) });
    elements.renameDialog.close();
    showToast("资料名称已更新");
    await loadFiles();
  } catch (requestError) {
    error.textContent = errorMessage(requestError, "重命名失败");
  } finally {
    setButtonLoading(button, false);
  }
}

function openProfileDialog() {
  const teacher = state.profile?.teacher || {};
  document.querySelector("#teacher-name-input").value = teacher.teacher_name || "";
  document.querySelector("#research-area-input").value = teacher.research_area || "";
  document.querySelector("#teacher-intro-input").value = teacher.intro || "";
  document.querySelector("#profile-form-error").textContent = "";
  elements.profileDialog.showModal();
}

async function saveProfile(event) {
  event.preventDefault();
  const button = document.querySelector("#save-profile-button");
  const error = document.querySelector("#profile-form-error");
  setButtonLoading(button, true, "保存中");
  try {
    await request(API.updateProfile, {
      method: "POST",
      body: JSON.stringify({ role: "teacher", teacher_name: document.querySelector("#teacher-name-input").value.trim(), research_area: document.querySelector("#research-area-input").value.trim(), intro: document.querySelector("#teacher-intro-input").value.trim() }),
    });
    await loadProfile();
    elements.profileDialog.close();
    showToast("教师资料已更新");
  } catch (requestError) {
    error.textContent = errorMessage(requestError, "资料保存失败");
  } finally {
    setButtonLoading(button, false);
  }
}

function openDeleteDialog(type, item) {
  state.pendingDelete = { type, item };
  document.querySelector("#delete-form-error").textContent = "";
  if (type === "course") {
    document.querySelector("#delete-title").textContent = "删除课程";
    document.querySelector("#delete-description").textContent = `确认删除“${item.course_name || "未命名课程"}”？课程内全部资料也会一并删除。`;
  } else {
    document.querySelector("#delete-title").textContent = "删除资料";
    document.querySelector("#delete-description").textContent = `确认删除“${item.file_name || "未命名资料"}”？文件与解析内容将一并移除。`;
  }
  elements.deleteDialog.showModal();
}

async function confirmDelete(event) {
  event.preventDefault();
  if (!state.pendingDelete) return;
  const { type, item } = state.pendingDelete;
  const button = document.querySelector("#confirm-delete-button");
  const error = document.querySelector("#delete-form-error");
  setButtonLoading(button, true, "删除中");
  try {
    const path = type === "course" ? API.deleteCourse : API.deleteFile;
    const id = type === "course" ? item.course_id : item.id;
    await request(path, { method: "POST", body: JSON.stringify({ id }) });
    elements.deleteDialog.close();
    state.pendingDelete = null;
    showToast(type === "course" ? "课程已删除" : "资料已删除");
    if (type === "course") {
      state.selectedCourse = null;
      await Promise.all([loadCourses(), loadAllFileCount()]);
    } else {
      await Promise.all([loadFiles(), loadAllFileCount()]);
    }
  } catch (requestError) {
    error.textContent = errorMessage(requestError, "删除失败");
  } finally {
    setButtonLoading(button, false);
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

function bindEvents() {
  document.querySelector("#alert-close").addEventListener("click", hideAlert);
  document.querySelector("#logout-button").addEventListener("click", logout);
  document.querySelector("#profile-button").addEventListener("click", openProfileDialog);
  document.querySelector("#add-course-button").addEventListener("click", () => openCourseDialog());
  document.querySelector("#edit-course-button").addEventListener("click", () => state.selectedCourse && openCourseDialog(state.selectedCourse));
  document.querySelector("#delete-course-button").addEventListener("click", () => state.selectedCourse && openDeleteDialog("course", state.selectedCourse));
  document.querySelector("#refresh-files-button").addEventListener("click", loadFiles);
  elements.courseForm.addEventListener("submit", saveCourse);
  elements.renameForm.addEventListener("submit", renameFile);
  elements.profileForm.addEventListener("submit", saveProfile);
  elements.deleteForm.addEventListener("submit", confirmDelete);
  elements.fileInput.addEventListener("change", () => setPendingFile(elements.fileInput.files?.[0]));
  elements.uploadButton.addEventListener("click", uploadFile);

  document.querySelector("#course-search-form").addEventListener("submit", (event) => {
    event.preventDefault();
    state.selectedCourse = null;
    loadCourses(document.querySelector("#course-search").value.trim()).catch((error) => showAlert(errorMessage(error, "课程搜索失败")));
  });
  document.querySelector("#file-search-form").addEventListener("submit", (event) => {
    event.preventDefault();
    state.fileSearch = document.querySelector("#file-search").value.trim();
    state.fileCurrent = 1;
    loadFiles();
  });
  document.querySelector("#previous-file-page").addEventListener("click", () => {
    if (state.fileCurrent > 1) { state.fileCurrent -= 1; loadFiles(); }
  });
  document.querySelector("#next-file-page").addEventListener("click", () => {
    if (state.fileCurrent < state.filePages) { state.fileCurrent += 1; loadFiles(); }
  });

  ["dragenter", "dragover"].forEach((name) => elements.uploadZone.addEventListener(name, (event) => {
    event.preventDefault();
    elements.uploadZone.classList.add("is-dragging");
  }));
  ["dragleave", "drop"].forEach((name) => elements.uploadZone.addEventListener(name, (event) => {
    event.preventDefault();
    elements.uploadZone.classList.remove("is-dragging");
  }));
  elements.uploadZone.addEventListener("drop", (event) => setPendingFile(event.dataTransfer?.files?.[0]));
  document.querySelectorAll(".modal-close, .cancel-button").forEach((button) => button.addEventListener("click", () => button.closest("dialog").close()));
}

async function initialize() {
  initializeIcons();
  bindEvents();
  try {
    await loadLoginUser();
    await Promise.all([loadProfile(), loadCourses(), loadAllFileCount()]);
  } catch (error) {
    const message = error?.message === "系统错误" ? "无法确认教师身份，请重新登录教师账号" : errorMessage(error, "教师工作台加载失败");
    document.querySelector("#user-name").textContent = "未登录";
    showAlert(message);
    elements.courseList.replaceChildren(createEmptyState("shield-alert", "无法进入教师工作台", message));
    initializeIcons();
  }
}

document.addEventListener("DOMContentLoaded", initialize);
