<!-- 忘记密码页(任务书 10.3):提交后提示"重置邮件已发送" -->
<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__head">
        <AppLogo />
        <h1 class="auth-card__title">忘记密码</h1>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" :show-label="false" size="large">
        <n-form-item path="email">
          <n-input v-model:value="form.email" placeholder="注册邮箱" clearable />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="onSubmit">发送重置邮件</n-button>
      </n-form>

      <div class="auth-card__links">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 提交 → POST /auth/forgot-password → 提示重置邮件已发送
import { ref } from 'vue'
import { NButton, NForm, NFormItem, NInput, useMessage, type FormInst, type FormRules } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import { forgotPassword } from '@/api/auth'

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const form = ref({ email: '' })

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: ['input', 'blur'] },
    { type: 'email', message: '邮箱格式不正确', trigger: ['input', 'blur'] },
  ],
}

async function onSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await forgotPassword(form.value.email)
    message.success('重置邮件已发送,请查收邮箱')
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

.auth-card__links {
  display: flex;
  justify-content: center;
  margin-top: 16px;
  font-size: 13px;
}

.auth-card__links a {
  color: var(--lc-primary);
  min-width: 44px;
  line-height: 44px;
  text-align: center;
}
</style>