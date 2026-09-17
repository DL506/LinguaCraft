<!-- 登录页(任务书 10.1):居中卡片 400px,登录成功跳 /dashboard -->
<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__head">
        <AppLogo />
        <h1 class="auth-card__title">登录 LinguaCraft</h1>
        <p class="auth-card__subtitle">广东专插本公共英语备考工具</p>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" :show-label="false" size="large">
        <n-form-item path="username">
          <n-input v-model:value="form.username" placeholder="用户名" clearable />
        </n-form-item>
        <n-form-item path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            show-password-on="click"
            placeholder="密码"
            @keydown.enter="onSubmit"
          />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="onSubmit">登录</n-button>
      </n-form>

      <div class="auth-card__links">
        <router-link to="/forgot-password">忘记密码?</router-link>
        <router-link to="/register">注册账号</router-link>
      </div>

      <p class="auth-card__hint">演示账号:demo / 123456</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 登录流程:校验 → POST /auth/login(api 层持久化 token)→ 写入用户 store → 跳首页
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NForm, NFormItem, NInput, useMessage, type FormInst, type FormRules } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const form = ref({ username: '', password: '' })

/** 表单校验规则 */
const rules: FormRules = {
  username: { required: true, message: '请输入用户名', trigger: ['input', 'blur'] },
  password: { required: true, message: '请输入密码', trigger: ['input', 'blur'] },
}

/** 提交登录 */
async function onSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const result = await login(form.value.username, form.value.password)
    userStore.setAuth(result.user)
    message.success('登录成功')
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
  justify-content: space-between;
  margin-top: 16px;
  font-size: 13px;
}

.auth-card__links a {
  color: var(--lc-primary);
  min-width: 44px;
  line-height: 44px;
}

.auth-card__hint {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--lc-text-3);
}
</style>