// 首页域 mock handlers(任务书 12.2)
// 今日任务完成度与刷题提交、背单词计数联动(演示动态数据)
import { http } from 'msw'
import { currentUser, delay, fail, ok } from './auth'
import { todaySubmissionsCount, userSettings } from './user'
import { vocabDoneCount } from './vocab'

export const homeHandlers = [
  /** 今日任务:目标读用户设置,完成数来自提交/背单词实时计数(12.2) */
  http.get('/api/home/today-tasks', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    return ok({
      items: [
        { type: 'reading', target: userSettings.dailyReadingGoal, done: todaySubmissionsCount('reading') },
        { type: 'cloze', target: 1, done: todaySubmissionsCount('cloze') },
        { type: 'grammar-fill', target: 1, done: todaySubmissionsCount('grammar-fill') },
        { type: 'vocab', target: userSettings.dailyWordGoal, done: vocabDoneCount },
      ],
    })
  }),

  /** 考试倒计时:天数按真实日期动态计算(12.2) */
  http.get('/api/home/exam-countdown', async ({ request }) => {
    await delay()
    if (!currentUser(request)) return fail(401, '未登录或登录已过期', 401)
    const examDate = '2027-03-20'
    const daysLeft = Math.max(0, Math.ceil((new Date(`${examDate}T09:00:00`).getTime() - Date.now()) / 86400000))
    return ok({ examDate, daysLeft, progress: 42 })
  }),
]