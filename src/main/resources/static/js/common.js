// 公共工具：API 封装与登录态管理
const TOKEN_KEY = 'ailife_token';
const USER_KEY = 'ailife_user';

function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || 'null');
  } catch (e) {
    return null;
  }
}

function setAuth(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

function requireLogin() {
  if (!getToken()) {
    location.href = 'login.html';
  }
}

function logout() {
  clearAuth();
  location.href = 'login.html';
}

// 统一请求封装
async function api(path, options = {}) {
  const opts = {
    method: options.method || 'GET',
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
  };
  if (getToken()) {
    opts.headers['Authorization'] = 'Bearer ' + getToken();
  }
  if (options.body !== undefined) {
    opts.body = JSON.stringify(options.body);
  }
  const resp = await fetch(path, opts);
  const data = await resp.json().catch(() => ({ code: 500, message: '响应解析失败' }));
  if (data.code === 401) {
    clearAuth();
    location.href = 'login.html';
    throw new Error(data.message || '登录已过期');
  }
  if (data.code !== 200) {
    throw new Error(data.message || '请求失败');
  }
  return data.data;
}

// 审核状态文案
const STATUS_MAP = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '人工复审' };
function statusText(status) {
  return STATUS_MAP[status] || '未知';
}
