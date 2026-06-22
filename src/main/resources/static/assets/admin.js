const API = Object.freeze({
  loginUser: "./user/get/login",
  logout: "./user/logout",
  listUsers: "./user/admin/list/page",
  addUser: "./user/admin/add",
  updateUser: "./user/admin/update",
  deleteUser: "./user/admin/delete",
  getProfile: "./user/admin/profile/get",
  updateProfile: "./user/admin/profile/update",
});

const state = { user: null, records: [], current: 1, pageSize: 10, total: 0, pages: 1, filters: { username: "", role: "" }, toastTimer: null };
const roleLabels = Object.freeze({ student: "学生", teacher: "教师", admin: "管理员", user: "普通用户" });

const elements = {
  alert: document.querySelector("#global-alert"), alertText: document.querySelector("#global-alert-text"),
  tableBody: document.querySelector("#user-table-body"), tableState: document.querySelector("#table-state"),
  previous: document.querySelector("#previous-page"), next: document.querySelector("#next-page"),
  accountDialog: document.querySelector("#account-dialog"), accountForm: document.querySelector("#account-form"),
  profileDialog: document.querySelector("#profile-dialog"), profileForm: document.querySelector("#profile-form"),
  deleteDialog: document.querySelector("#delete-dialog"), deleteForm: document.querySelector("#delete-form"),
  toast: document.querySelector("#toast"), toastText: document.querySelector("#toast-text"),
};

class ApiError extends Error {
  constructor(message, code, status) { super(message); this.name = "ApiError"; this.code = code; this.status = status; }
}

function initializeIcons() { if (window.lucide) window.lucide.createIcons(); }
function apiUrl(path) { return new URL(path, document.baseURI).toString(); }
function roleLabel(role) { return roleLabels[role] || "未知身份"; }
function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : new Intl.DateTimeFormat("zh-CN", { year: "numeric", month: "2-digit", day: "2-digit" }).format(date);
}

async function request(path, options = {}) {
  const response = await fetch(apiUrl(path), {
    credentials: "same-origin",
    headers: { Accept: "application/json", ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers },
    ...options,
  });
  let payload;
  try { payload = await response.json(); } catch { throw new ApiError("服务器返回了无法识别的响应", null, response.status); }
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
function errorMessage(error, fallback) { return error instanceof TypeError ? "无法连接服务器，请确认后端服务已启动" : error?.message || fallback; }
function showAlert(message) { elements.alertText.textContent = message; elements.alert.hidden = false; }
function hideAlert() { elements.alert.hidden = true; elements.alertText.textContent = ""; }
function showToast(message) {
  window.clearTimeout(state.toastTimer);
  elements.toastText.textContent = message;
  elements.toast.hidden = false;
  state.toastTimer = window.setTimeout(() => { elements.toast.hidden = true; }, 2600);
}
function setButtonLoading(button, loading, label = "处理中") {
  if (loading) { button.dataset.html = button.innerHTML; button.disabled = true; button.textContent = label; }
  else { button.disabled = false; if (button.dataset.html) { button.innerHTML = button.dataset.html; delete button.dataset.html; initializeIcons(); } }
}
function avatarContent(container, user, large = false) {
  container.replaceChildren();
  if (user?.avatar) {
    const image = document.createElement("img"); image.src = user.avatar; image.alt = `${user.username || "用户"}的头像`; container.append(image);
  } else {
    const span = document.createElement("span"); span.textContent = (user?.username || "U").slice(0, 1).toUpperCase(); container.append(span);
  }
  container.classList.toggle("avatar-large", large);
}

async function loadLoginUser() {
  const user = await request(API.loginUser);
  if (user.role !== "admin") throw new ApiError("当前账号没有管理员权限", 40101, 403);
  state.user = user;
  sessionStorage.setItem("studyAi.loginUser", JSON.stringify(user));
  document.querySelector("#user-name").textContent = user.username || "管理员";
  avatarContent(document.querySelector("#user-avatar"), user);
}

async function fetchUserPage(overrides = {}) {
  const username = overrides.username ?? state.filters.username;
  const role = overrides.role ?? state.filters.role;
  return request(API.listUsers, {
    method: "POST",
    body: JSON.stringify({
      current: overrides.current ?? state.current,
      pageSize: overrides.pageSize ?? state.pageSize,
      username: username || undefined,
      role: role || undefined,
    }),
  });
}

function createState(icon, title, description, loading = false) {
  const wrapper = document.createElement("div"); wrapper.className = "state-content";
  if (loading) { const spinner = document.createElement("span"); spinner.className = "spinner"; wrapper.append(spinner); }
  else { const iconElement = document.createElement("i"); iconElement.dataset.lucide = icon; iconElement.setAttribute("aria-hidden", "true"); wrapper.append(iconElement); }
  const strong = document.createElement("strong"); strong.textContent = title;
  const paragraph = document.createElement("p"); paragraph.textContent = description;
  wrapper.append(strong, paragraph); return wrapper;
}

function renderUsers() {
  elements.tableBody.replaceChildren(); elements.tableState.replaceChildren();
  if (!state.records.length) {
    elements.tableState.append(createState("user-search", "没有找到账号", "调整搜索词或身份筛选后重试。")); initializeIcons(); return;
  }
  state.records.forEach((user) => {
    const row = document.createElement("tr");
    const account = document.createElement("td");
    const accountCell = document.createElement("div"); accountCell.className = "account-cell";
    const avatar = document.createElement("span"); avatar.className = "avatar"; avatarContent(avatar, user);
    const copy = document.createElement("div"); const name = document.createElement("strong"); name.textContent = user.username || "未命名账号";
    const updated = document.createElement("small"); updated.textContent = `更新于 ${formatDate(user.updateTime)}`; copy.append(name, updated); accountCell.append(avatar, copy); account.append(accountCell);
    const role = document.createElement("td"); const badge = document.createElement("span"); badge.className = `role-badge ${user.role || "user"}`; badge.textContent = roleLabel(user.role); role.append(badge);
    const id = document.createElement("td"); id.textContent = user.id ?? "—";
    const created = document.createElement("td"); created.textContent = formatDate(user.createTime);
    const actions = document.createElement("td"); const actionGroup = document.createElement("div"); actionGroup.className = "row-actions";
    actionGroup.append(createActionButton("id-card", "维护身份资料", () => openProfile(user)), createActionButton("pencil", "编辑账号", () => openAccountDialog(user)));
    const deleteButton = createActionButton("trash-2", "删除账号", () => openDeleteDialog(user)); deleteButton.classList.add("delete-button"); deleteButton.disabled = user.id === state.user?.id; actionGroup.append(deleteButton);
    actions.append(actionGroup); row.append(account, role, id, created, actions); elements.tableBody.append(row);
  });
  initializeIcons();
}

function createActionButton(icon, label, handler) {
  const button = document.createElement("button"); button.type = "button"; button.className = "icon-button"; button.setAttribute("aria-label", label); button.title = label;
  const iconElement = document.createElement("i"); iconElement.dataset.lucide = icon; iconElement.setAttribute("aria-hidden", "true"); button.append(iconElement); button.addEventListener("click", handler); return button;
}

function updatePagination() {
  document.querySelector("#page-indicator").textContent = `${state.current} / ${state.pages}`;
  document.querySelector("#page-summary").textContent = `共 ${state.total} 个账号，第 ${state.current} 页`;
  elements.previous.disabled = state.current <= 1; elements.next.disabled = state.current >= state.pages;
}

async function loadUsers() {
  elements.tableBody.replaceChildren(); elements.tableState.replaceChildren(createState("", "正在加载账号", "正在读取平台账号目录。", true)); initializeIcons();
  try {
    const page = await fetchUserPage();
    state.records = Array.isArray(page?.records) ? page.records : []; state.total = Number(page?.total) || 0; state.pages = Math.max(1, Number(page?.pages) || Math.ceil(state.total / state.pageSize) || 1);
    if (state.current > state.pages) { state.current = state.pages; return loadUsers(); }
    renderUsers(); updatePagination();
  } catch (error) {
    elements.tableBody.replaceChildren(); elements.tableState.replaceChildren(createState("circle-alert", "账号加载失败", errorMessage(error, "无法读取账号目录"))); initializeIcons(); showAlert(errorMessage(error, "账号加载失败"));
  }
}

async function loadMetrics() {
  const roles = ["", "student", "teacher", "admin"];
  try {
    const pages = await Promise.all(roles.map((role) => fetchUserPage({ current: 1, pageSize: 1, username: "", role })));
    ["total", "student", "teacher", "admin"].forEach((key, index) => { document.querySelector(`#metric-${key}`).textContent = pages[index]?.total ?? 0; });
  } catch { ["total", "student", "teacher", "admin"].forEach((key) => { document.querySelector(`#metric-${key}`).textContent = "—"; }); }
}

function openAccountDialog(user = null) {
  elements.accountForm.reset(); document.querySelector("#account-form-error").textContent = "";
  const editing = Boolean(user); document.querySelector("#account-id").value = user?.id ?? ""; document.querySelector("#account-username").value = user?.username || ""; document.querySelector("#account-role").value = user?.role || "student"; document.querySelector("#account-avatar").value = user?.avatar || "";
  const password = document.querySelector("#account-password"); password.required = !editing; password.value = ""; password.placeholder = editing ? "留空表示不修改" : "请输入初始密码";
  document.querySelector("#password-hint").textContent = editing ? "留空保留原密码；新密码至少 8 个字符" : "至少 8 个字符";
  document.querySelector("#account-dialog-title").textContent = editing ? "编辑账号" : "新增账号"; document.querySelector("#account-dialog-code").textContent = editing ? "EDIT ACCOUNT" : "NEW ACCOUNT";
  elements.accountDialog.showModal(); document.querySelector("#account-username").focus();
}

async function saveAccount(event) {
  event.preventDefault(); const id = document.querySelector("#account-id").value; const username = document.querySelector("#account-username").value.trim(); const password = document.querySelector("#account-password").value; const role = document.querySelector("#account-role").value; const avatar = document.querySelector("#account-avatar").value.trim(); const error = document.querySelector("#account-form-error");
  if (username.length < 4) { error.textContent = "账号至少需要 4 个字符"; return; }
  if ((!id && password.length < 8) || (password && password.length < 8)) { error.textContent = "密码至少需要 8 个字符"; return; }
  const button = document.querySelector("#save-account-button"); setButtonLoading(button, true, "保存中"); error.textContent = "";
  try {
    const payload = { username, role, ...(avatar ? { avatar } : {}), ...(password ? { password } : {}) };
    if (id) { payload.id = Number(id); await request(API.updateUser, { method: "POST", body: JSON.stringify(payload) }); }
    else { await request(API.addUser, { method: "POST", body: JSON.stringify(payload) }); }
    elements.accountDialog.close(); showToast(id ? "账号已更新" : "账号已创建"); await Promise.all([loadUsers(), loadMetrics()]);
  } catch (requestError) { error.textContent = errorMessage(requestError, "账号保存失败"); }
  finally { setButtonLoading(button, false); }
}

function setProfileAvatar(user) { avatarContent(document.querySelector("#profile-avatar"), user, true); }
async function openProfile(user) {
  document.querySelector("#profile-form-error").textContent = ""; document.querySelector("#profile-dialog-title").textContent = "身份资料"; document.querySelector("#profile-username").textContent = user.username || "未命名账号";
  const badge = document.querySelector("#profile-role-badge"); badge.className = `role-badge ${user.role || "user"}`; badge.textContent = roleLabel(user.role); setProfileAvatar(user);
  document.querySelector("#profile-user-id").value = user.id; document.querySelector("#profile-role").value = user.role;
  document.querySelector("#student-profile-fields").hidden = true; document.querySelector("#teacher-profile-fields").hidden = true; document.querySelector("#profile-empty").hidden = true; document.querySelector("#save-profile-button").hidden = !["student", "teacher"].includes(user.role);
  elements.profileDialog.showModal();
  try {
    const profile = await request(`${API.getProfile}?userId=${encodeURIComponent(user.id)}`); fillProfile(profile || { user });
  } catch (error) { document.querySelector("#profile-form-error").textContent = errorMessage(error, "身份资料加载失败"); }
}

function fillProfile(profile) {
  const user = profile.user || {}; const role = user.role || document.querySelector("#profile-role").value; document.querySelector("#profile-role").value = role; setProfileAvatar(user);
  if (role === "student") {
    const student = profile.student || {}; document.querySelector("#student-profile-fields").hidden = false;
    document.querySelector("#profile-major").value = student.major || ""; document.querySelector("#profile-grade").value = student.grade || ""; document.querySelector("#profile-learning-target").value = student.learning_target || ""; document.querySelector("#profile-interest").value = student.interest_direction || ""; document.querySelector("#profile-knowledge").value = student.knowledge_level || "";
  } else if (role === "teacher") {
    const teacher = profile.teacher || {}; document.querySelector("#teacher-profile-fields").hidden = false;
    document.querySelector("#profile-teacher-name").value = teacher.teacher_name || ""; document.querySelector("#profile-research-area").value = teacher.research_area || ""; document.querySelector("#profile-intro").value = teacher.intro || "";
  } else document.querySelector("#profile-empty").hidden = false;
  initializeIcons();
}

async function saveProfile(event) {
  event.preventDefault(); const userId = Number(document.querySelector("#profile-user-id").value); const role = document.querySelector("#profile-role").value; const error = document.querySelector("#profile-form-error");
  if (!["student", "teacher"].includes(role)) { elements.profileDialog.close(); return; }
  const payload = role === "student"
    ? { userId, role, major: document.querySelector("#profile-major").value.trim(), grade: document.querySelector("#profile-grade").value.trim(), learning_target: document.querySelector("#profile-learning-target").value.trim(), interest_direction: document.querySelector("#profile-interest").value.trim(), knowledge_level: document.querySelector("#profile-knowledge").value.trim() }
    : { userId, role, teacher_name: document.querySelector("#profile-teacher-name").value.trim(), research_area: document.querySelector("#profile-research-area").value.trim(), intro: document.querySelector("#profile-intro").value.trim() };
  const button = document.querySelector("#save-profile-button"); setButtonLoading(button, true, "保存中"); error.textContent = "";
  try { await request(API.updateProfile, { method: "POST", body: JSON.stringify(payload) }); elements.profileDialog.close(); showToast("身份资料已更新"); await loadUsers(); }
  catch (requestError) { error.textContent = errorMessage(requestError, "资料保存失败"); }
  finally { setButtonLoading(button, false); }
}

function openDeleteDialog(user) {
  if (user.id === state.user?.id) return; document.querySelector("#delete-user-id").value = user.id; document.querySelector("#delete-username").textContent = user.username || `ID ${user.id}`; document.querySelector("#delete-form-error").textContent = ""; elements.deleteDialog.showModal();
}
async function deleteUser(event) {
  event.preventDefault(); const id = Number(document.querySelector("#delete-user-id").value); const error = document.querySelector("#delete-form-error"); const button = document.querySelector("#confirm-delete-button"); setButtonLoading(button, true, "删除中");
  try { await request(API.deleteUser, { method: "POST", body: JSON.stringify({ id }) }); elements.deleteDialog.close(); showToast("账号已删除"); await Promise.all([loadUsers(), loadMetrics()]); }
  catch (requestError) { error.textContent = errorMessage(requestError, "账号删除失败"); }
  finally { setButtonLoading(button, false); }
}

async function logout() {
  const button = document.querySelector("#logout-button"); button.disabled = true;
  try { await request(API.logout, { method: "POST" }); } catch (error) { if (error?.code !== 40100) { showAlert(errorMessage(error, "退出登录失败")); button.disabled = false; return; } }
  sessionStorage.removeItem("studyAi.loginUser"); window.location.replace("./login.html");
}

function bindEvents() {
  document.querySelector("#alert-close").addEventListener("click", hideAlert); document.querySelector("#logout-button").addEventListener("click", logout);
  document.querySelector("#add-user-button").addEventListener("click", () => openAccountDialog()); document.querySelector("#refresh-button").addEventListener("click", () => Promise.all([loadUsers(), loadMetrics()]));
  document.querySelector("#filter-form").addEventListener("submit", (event) => { event.preventDefault(); state.filters.username = document.querySelector("#user-search").value.trim(); state.filters.role = document.querySelector("#role-filter").value; state.current = 1; loadUsers(); });
  elements.previous.addEventListener("click", () => { if (state.current > 1) { state.current -= 1; loadUsers(); } }); elements.next.addEventListener("click", () => { if (state.current < state.pages) { state.current += 1; loadUsers(); } });
  elements.accountForm.addEventListener("submit", saveAccount); elements.profileForm.addEventListener("submit", saveProfile); elements.deleteForm.addEventListener("submit", deleteUser);
  document.querySelectorAll(".modal-close, .cancel-button").forEach((button) => button.addEventListener("click", () => button.closest("dialog").close()));
}

async function initialize() {
  initializeIcons(); bindEvents();
  try { await loadLoginUser(); await Promise.all([loadUsers(), loadMetrics()]); }
  catch (error) {
    const message = error?.message === "系统错误" ? "无法确认管理员身份，请重新登录管理员账号" : errorMessage(error, "请使用管理员账号登录");
    document.querySelector("#user-name").textContent = "未登录";
    showAlert(message);
    elements.tableState.replaceChildren(createState("shield-alert", "无法进入管理员工作台", message));
    initializeIcons();
  }
}

document.addEventListener("DOMContentLoaded", initialize);
