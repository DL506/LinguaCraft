<!-- 设置(任务书 10.15):桌面两列表单 / 手机单列
     主题三选一(写 localStorage,不入服务端)、大小写敏感、批改方式、学习目标、口音、提醒时间(gradingMethod/grammarFillCaseSensitive 为服务端生效偏好,12.7 说明) -->
<template>
  <div class="settings-page">
    <div class="settings-page__grid" :class="{ 'is-mobile': isMobile }">
      <!-- 主题模式(本地偏好,5.3) -->
      <div class="setting-row">
        <span class="setting-row__label">主题模式</span>
        <div class="setting-row__control">
          <n-radio-group :value="appStore.themeMode" @update:value="onThemeChange">
            <n-radio-button value="system">跟随系统</n-radio-button>
            <n-radio-button value="light">亮色</n-radio-button>
            <n-radio-button value="dark">暗色</n-radio-button>
          </n-radio-group>
        </div>
      </div>

      <!-- 语法填空大小写敏感 -->
      <div class="setting-row">
        <span class="setting-row__label">语法填空区分大小写</span>
        <div class="setting-row__control">
          <n-switch v-model:value="form.grammarFillCaseSensitive" />
          <span class="setting-row__hint">关闭后 unfortunately 与 Unfortunately 视为相同</span>
        </div>
      </div>

      <!-- 批改方式(10.15 UI) -->
      <div class="setting-row">
        <span class="setting-row__label">批改方式</span>
        <div class="setting-row__control">
          <n-radio-group v-model:value="form.gradingMethod">
            <n-radio value="ai">AI 批改</n-radio>
            <n-radio value="manual">人工批改</n-radio>
          </n-radio-group>
          <span class="setting-row__hint">
            {{ form.gradingMethod === 'ai' ? '提交后即时出结果,含判分和解析' : '稍后出结果,选择题优先数据库比对,作文由真人批改' }}
          </span>
        </div>
      </div>

      <!-- 每日背单词目标 -->
      <div class="setting-row">
        <span class="setting-row__label">每日背单词目标</span>
        <div class="setting-row__control">
          <n-input-number v-model:value="form.dailyWordGoal" :min="5" :max="300" :step="5" />
          <span class="setting-row__hint">词</span>
        </div>
      </div>

      <!-- 每日阅读目标 -->
      <div class="setting-row">
        <span class="setting-row__label">每日阅读目标</span>
        <div class="setting-row__control">
          <n-input-number v-model:value="form.dailyReadingGoal" :min="1" :max="10" />
          <span class="setting-row__hint">篇</span>
        </div>
      </div>

      <!-- 发音口音 -->
      <div class="setting-row">
        <span class="setting-row__label">发音口音</span>
        <div class="setting-row__control">
          <n-select
            v-model:value="form.accent"
            class="setting-row__select"
            :options="[
              { label: '美式发音', value: 'us' },
              { label: '英式发音', value: 'uk' },
            ]"
          />
        </div>
      </div>

      <!-- 学习提醒时间 -->
      <div class="setting-row">
        <span class="setting-row__label">学习提醒时间</span>
        <div class="setting-row__control">
          <n-input v-model:value="form.reminderTime" class="setting-row__time" placeholder="HH:mm" />
          <span class="setting-row__hint">如 20:00,留空表示不提醒</span>
        </div>
      </div>
    </div>

    <!-- 保存 -->
    <div class="settings-page__actions">
      <n-button type="primary" :loading="saving" @click="onSave">保存设置</n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 数据:GET /user/settings 初始化表单;PUT /user/settings 保存(未提供字段保持不变)
// themeMode 仅写 localStorage(任务书 5.3,权威源本地)
import { onMounted, ref } from 'vue'
import { NButton, NInput, NInputNumber, NRadio, NRadioButton, NRadioGroup, NSelect, NSwitch, useMessage } from 'naive-ui'
import { getUserSettings, updateUserSettings } from '@/api/user'
import { useDevice } from '@/composables/useDevice'
import { useAppStore, type ThemeMode } from '@/stores/app'
import type { UserSettings } from '@/types/user'

const message = useMessage()
const { isMobile } = useDevice()
const appStore = useAppStore()

const saving = ref(false)

/** 表单(服务端设置项;themeMode 除外) */
const form = ref<UserSettings>({
  dailyWordGoal: 50,
  dailyReadingGoal: 1,
  accent: 'us',
  grammarFillCaseSensitive: false,
  gradingMethod: 'ai',
  reminderTime: '',
})

/** 主题切换:立即生效并持久化到 localStorage(不入服务端) */
function onThemeChange(mode: string | number): void {
  appStore.setThemeMode(mode as ThemeMode)
}

/** 保存到服务端 */
async function onSave(): Promise<void> {
  saving.value = true
  try {
    await updateUserSettings({ ...form.value })
    message.success('设置已保存')
  } catch {
    // 错误提示由 request 层统一弹出
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const settings = await getUserSettings()
  form.value = { ...settings, reminderTime: settings.reminderTime ?? '' }
})
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

/* 桌面两列;手机单列 */
.settings-page__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.settings-page__grid.is-mobile {
  grid-template-columns: minmax(0, 1fr);
}

.setting-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border-radius: var(--lc-radius-lg);
  background-color: var(--lc-bg-card);
  min-width: 0;
}

.setting-row__label {
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-text-1);
}

.setting-row__control {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.setting-row__hint {
  font-size: 12px;
  color: var(--lc-text-3);
  overflow-wrap: break-word;
}

.setting-row__select {
  width: 160px;
}

.setting-row__time {
  width: 120px;
}

.settings-page__actions {
  display: flex;
  justify-content: flex-end;
}
</style>