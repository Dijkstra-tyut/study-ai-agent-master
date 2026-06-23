const LOGIN_ENDPOINT = new URL("./user/login", document.baseURI).toString();
const REGISTER_ENDPOINT = new URL("./user/register", document.baseURI).toString();
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
const loginModeButton = document.querySelector("#login-mode-button");
const registerModeButton = document.querySelector("#register-mode-button");
const usernameInput = document.querySelector("#username");
const passwordInput = document.querySelector("#password");
const checkPasswordField = document.querySelector("#check-password-field");
const checkPasswordInput = document.querySelector("#check-password");
const rememberInput = document.querySelector("#remember-username");
const passwordToggle = document.querySelector("#password-toggle");
const submitButton = document.querySelector("#submit-button");
const alertBox = document.querySelector("#form-alert");
const alertText = document.querySelector("#form-alert-text");
const successPanel = document.querySelector("#success-panel");
let mode = "login";

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
  const checkPassword = checkPasswordInput.value;
  let valid = true;

  setFieldError(usernameInput, "");
  setFieldError(passwordInput, "");
  setFieldError(checkPasswordInput, "");

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

  if (mode === "register") {
    if (!checkPassword) {
      setFieldError(checkPasswordInput, "请再次输入密码");
      valid = false;
    } else if (checkPassword.length < 8) {
      setFieldError(checkPasswordInput, "确认密码至少需要 8 个字符");
      valid = false;
    } else if (checkPassword !== password) {
      setFieldError(checkPasswordInput, "两次输入的密码不一致");
      valid = false;
    }
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

function setMode(nextMode) {
  mode = nextMode;
  const isRegister = mode === "register";
  loginModeButton.classList.toggle("is-active", !isRegister);
  registerModeButton.classList.toggle("is-active", isRegister);
  loginModeButton.setAttribute("aria-selected", String(!isRegister));
  registerModeButton.setAttribute("aria-selected", String(isRegister));
  checkPasswordField.hidden = !isRegister;
  checkPasswordInput.required = isRegister;
  passwordInput.autocomplete = isRegister ? "new-password" : "current-password";
  document.querySelector("#mode-pill-text").textContent = isRegister ? "创建账号" : "安全登录";
  document.querySelector("#login-title").textContent = isRegister ? "创建新账号" : "欢迎回来";
  document.querySelector("#login-subtitle").textContent = isRegister
    ? "自助创建的账号默认为普通用户，管理员可在控制台调整为学生、教师或管理员。"
    : "使用你的平台账号登录，系统会自动识别学生、教师或管理员身份。";
  document.querySelector(".button-label").textContent = isRegister ? "创建并登录" : "登录";
  document.querySelector("#form-footer-text").textContent = isRegister
    ? "创建成功后将自动登录；如需进入教师或管理员工作台，请联系管理员调整身份。"
    : "管理员可在控制台创建教师、学生和管理员账号；普通用户也可先自助创建基础账号。";
  hideAlert();
  setFieldError(usernameInput, "");
  setFieldError(passwordInput, "");
  setFieldError(checkPasswordInput, "");
}

async function postJson(endpoint, body) {
  const response = await fetch(endpoint, {
    method: "POST",
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  let payload;
  try {
    payload = await response.json();
  } catch {
    throw new Error("服务器返回了无法识别的响应");
  }

  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || "请求失败");
  }

  return payload.data;
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
    if (mode === "register") {
      await postJson(REGISTER_ENDPOINT, {
        username: usernameInput.value.trim(),
        password: passwordInput.value,
        checkPassword: checkPasswordInput.value,
      });
    }

    const user = await postJson(LOGIN_ENDPOINT, {
      username: usernameInput.value.trim(),
      password: passwordInput.value,
    });

    if (!user) {
      throw new Error("账号或密码错误");
    }

    completeLogin(user);
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
checkPasswordInput.addEventListener("input", () => setFieldError(checkPasswordInput, ""));
passwordToggle.addEventListener("click", togglePasswordVisibility);
loginModeButton.addEventListener("click", () => setMode("login"));
registerModeButton.addEventListener("click", () => setMode("register"));
form.addEventListener("submit", submitLogin);

document.addEventListener("DOMContentLoaded", () => {
  initializeIcons();
  restoreRememberedUsername();
});
