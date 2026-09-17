<!-- 注册页(任务书 10.2):与登录同风格;注册即登录,成功后跳 /dashboard -->
<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__head">
        <AppLogo />
        <h1 class="auth-card__title">注册 LinguaCraft</h1>
        <p class="auth-card__subtitle">广东专插本公共英语备考工具</p>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" :show-label="false" size="large">
        <n-form-item path="username">
          <n-input v-model:value="form.username" placeholder="用户名(3~20 位字母数字)" clearable />
        </n-form-item>
        <n-form-item path="email">
          <n-input v-model:value="form.email" placeholder="邮箱(用于找回密码)" clearable />
        </n-form-item>
        <n-form-item path="nickname">
          <n-input v-model:value="form.nickname" placeholder="昵称" clearable />
        </n-form-item>
        <n-form-item path="password">
          <n-input v-model:value="form.password" type="password" placeholder="密码(至少 6 位)" />
        </n-form-item>
        <n-form-item path="confirmPassword">
          <n-input v-model:value="form.confirmPassword" type="password" placeholder="确认密码" @keydown.enter="onSubmit" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="onSubmit">注册并登录</n-button>
      </n-form>

      <div class="auth-card__links">
        <span>已有账号?</span>
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 注册流程:校验 → POST /auth/register(注册即登录,token 由 api 层持久化)→ 写 store → 跳首页
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NForm, NFormItem, NInput, useMessage, type FormInst, type FormRules } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import { register } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const form = ref({ username: '', email: '', nickname: '', password: '', confirmPassword: '' })

/** 表单校验规则(字段约束按任务书 10.2) */
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
    // 3~20 位字母数字
    { pattern: /^[a-zA-Z0-9]{3,20}$/, message: '用户名须为 3~20 位字母数字', trigger: ['input', 'blur'] },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: ['input', 'blur'] },
    { type: 'email', message: '邮箱格式不正确', trigger: ['input', 'blur'] },
  ],
  nickname: { required: true, message: '请输入昵称', trigger: ['input', 'blur'] },
  password: [
    { required: true, message: '请输入密码', trigger: ['input', 'blur'] },
    { min: 6, message: '密码至少 6 位', trigger: ['input', 'blur'] },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: ['input', 'blur'] },
    {
      validator: (_rule: unknown, value: string) => value === form.value.password || new Error('两次输入的密码不一致'),
      trigger: ['input', 'blur'],
    },
  ],
}

/** 提交注册 */
async function onSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await register({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname,
      email: form.value.email,
    })
    userStore.setAuth(result.user)
    message.success('注册成功,已自动登录')
    router.push('/dashboard')
  } catch {
    // 错误提示由 request 层统一弹出
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
}

.auth-card {
  width: min(400px, 100%);
  padding: 32px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  box-shadow: var(--lc-shadow);
}

.auth-card__head {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.auth-card__title {
  font-size: 20px;
  color: var(--lc-text-1);
}

.auth-card__subtitle {
  font-size: 13px;
  color: var(--lc-text-3);
}

.auth-card__links {
  display: flex;
  justify-content: center;
  gap: 8px;
  align-items: center;
  margin-top: 16px;
  font-size: 13px;
  color: var(--lc-text-3);
}

.auth-card__links a {
  color: var(--lc-primary);
  min-width: 44px;
  line-height: 44px;
  text-align: center;
}
</style>