<!-- 重置密码页(任务书 10.4):token 来自 URL query,成功后跳 /login
     注:token 放 URL query 会进入历史与访问日志,MVP 可接受;后端阶段建议一次性 code(任务书 10.4 注) -->
<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-card__head">
        <AppLogo />
        <h1 class="auth-card__title">重置密码</h1>
      </div>

      <n-form ref="formRef" :model="form" :rules="rules" :show-label="false" size="large">
        <n-form-item path="password">
          <n-input v-model:value="form.password" type="password" placeholder="新密码(至少 6 位)" />
        </n-form-item>
        <n-form-item path="confirmPassword">
          <n-input v-model:value="form.confirmPassword" type="password" placeholder="确认密码" @keydown.enter="onSubmit" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="onSubmit">确认重置</n-button>
      </n-form>

      <div class="auth-card__links">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 提交 → POST /auth/reset-password(body 带 URL query 的 token)→ 成功跳 /login
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NForm, NFormItem, NInput, useMessage, type FormInst, type FormRules } from 'naive-ui'
import AppLogo from '@/components/common/AppLogo.vue'
import { resetPassword } from '@/api/auth'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const form = ref({ password: '', confirmPassword: '' })

/** 校验规则:新密码 ≥6 位,两次一致 */
const rules: FormRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: ['input', 'blur'] },
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

async function onSubmit(): Promise<void> {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  const token = String(route.query.token ?? '')
  if (!token) {
    message.error('缺少重置凭证,请从邮件链接进入')
    return
  }
  loading.value = true
  try {
    await resetPassword(token, form.value.password)
    message.success('密码已重置,请重新登录')
    router.push('/login')
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