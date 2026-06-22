const LOGIN_ENDPOINT = new URL("./user/login", document.baseURI).toString();
const ROLE_LABELS = Object.freeze({
  student: "学生",
  teacher: "教师",
  admin: "管理员",
});
const ROLE_ROUTES = Object.freeze({
  student: "./student.html",
  teacher: "./teacher.html",
  admin: "./admin.html",
});

const form = document.querySelector("#login-form");
const usernameInput = document.querySelector("#username");
const passwordInput = document.querySelector("#password");
const rememberInput = document.querySelector("#remember-username");
const passwordToggle = document.querySelector("#password-toggle");
const submitButton = document.querySelector("#submit-button");
const alertBox = document.querySelector("#form-alert");
const alertText = document.querySelector("#form-alert-text");
const successPanel = document.querySelector("#success-panel");

function initializeIcons() {
  if (window.lucide) {
    window.lucide.createIcons();
  }
}

function getFieldParts(input) {
  return {
    shell: input.closest(".input-shell"),
    error: document.querySelector(`#${input.id}-error`),
  };
}

function setFieldError(input, message) {
  const { shell, error } = getFieldParts(input);
  shell?.classList.toggle("is-invalid", Boolean(message));
  input.setAttribute("aria-invalid", String(Boolean(message)));
  if (error) {
    error.textContent = message;
  }
}

function validateForm() {
  const username = usernameInput.value.trim();
  const password = passwordInput.value;
  let valid = true;

  setFieldError(usernameInput, "");
  setFieldError(passwordInput, "");

  if (!username) {
    setFieldError(usernameInput, "请输入账号");
    valid = false;
  } else if (username.length < 4) {
    setFieldError(usernameInput, "账号至少需要 4 个字符");
    valid = false;
  }

  if (!password) {
    setFieldError(passwordInput, "请输入密码");
    valid = false;
  } else if (password.length < 8) {
    setFieldError(passwordInput, "密码至少需要 8 个字符");
    valid = false;
  }

  return valid;
}

function setLoading(loading) {
  submitButton.disabled = loading;
  submitButton.classList.toggle("is-loading", loading);
  submitButton.setAttribute("aria-busy", String(loading));
}

function showAlert(message) {
  alertText.textContent = message;
  alertBox.hidden = false;
}

function hideAlert() {
  alertBox.hidden = true;
  alertText.textContent = "";
}

function getSafeRedirect() {
  const redirect = new URLSearchParams(window.location.search).get("redirect");
  if (!redirect) {
    return null;
  }

  try {
    const target = new URL(redirect, window.location.origin);
    const loginPath = new URL(window.location.href).pathname;
    if (target.origin === window.location.origin && target.pathname !== loginPath) {
      return target.toString();
    }
  } catch {
    return null;
  }

  return null;
}

function completeLogin(user) {
  const role = ROLE_LABELS[user.role] || "平台用户";
  sessionStorage.setItem("studyAi.loginUser", JSON.stringify(user));

  if (rememberInput.checked) {
    localStorage.setItem("studyAi.rememberedUsername", user.username);
  } else {
    localStorage.removeItem("studyAi.rememberedUsername");
  }

  const roleRoute = ROLE_ROUTES[user.role];
  const redirect = getSafeRedirect();
  if (redirect && roleRoute) {
    const redirectUrl = new URL(redirect);
    const roleUrl = new URL(roleRoute, document.baseURI);
    if (redirectUrl.pathname === roleUrl.pathname) {
      window.location.assign(redirect);
      return;
    }
  }

  if (roleRoute) {
    window.location.assign(roleRoute);
    return;
  }

  document.querySelector("#success-username").textContent = user.username;
  document.querySelector("#success-role").textContent = role;
  form.hidden = true;
  successPanel.hidden = false;
}

async function submitLogin(event) {
  event.preventDefault();
  hideAlert();

  if (!validateForm()) {
    const firstInvalid = form.querySelector('[aria-invalid="true"]');
    firstInvalid?.focus();
    return;
  }

  setLoading(true);

  try {
    const response = await fetch(LOGIN_ENDPOINT, {
      method: "POST",
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        username: usernameInput.value.trim(),
        password: passwordInput.value,
      }),
    });

    let payload;
    try {
      payload = await response.json();
    } catch {
      throw new Error("服务器返回了无法识别的响应");
    }

    if (!response.ok || payload.code !== 0 || !payload.data) {
      throw new Error(payload.message || "账号或密码错误");
    }

    completeLogin(payload.data);
  } catch (error) {
    const message = error instanceof TypeError ? "无法连接服务器，请确认后端服务已启动" : error.message;
    showAlert(message || "登录失败，请稍后重试");
  } finally {
    setLoading(false);
  }
}

function togglePasswordVisibility() {
  const willShow = passwordInput.type === "password";
  passwordInput.type = willShow ? "text" : "password";
  passwordToggle.setAttribute("aria-label", willShow ? "隐藏密码" : "显示密码");
  passwordToggle.setAttribute("aria-pressed", String(willShow));
  passwordToggle.innerHTML = `<i data-lucide="${willShow ? "eye-off" : "eye"}" aria-hidden="true"></i>`;
  initializeIcons();
}

function restoreRememberedUsername() {
  const remembered = localStorage.getItem("studyAi.rememberedUsername");
  if (remembered) {
    usernameInput.value = remembered;
    rememberInput.checked = true;
    passwordInput.focus();
  }
}

usernameInput.addEventListener("input", () => setFieldError(usernameInput, ""));
passwordInput.addEventListener("input", () => setFieldError(passwordInput, ""));
passwordToggle.addEventListener("click", togglePasswordVisibility);
form.addEventListener("submit", submitLogin);

document.addEventListener("DOMContentLoaded", () => {
  initializeIcons();
  restoreRememberedUsername();
});
