// ESLint 配置(legacy 格式):Vue3 + TypeScript 推荐规则
module.exports = {
  root: true,
  env: {
    browser: true,
    es2022: true,
    node: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
    'plugin:vue/vue3-recommended',
  ],
  // vue-eslint-parser 解析 .vue 文件,内部委托 @typescript-eslint/parser 解析 script 块
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 2022,
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint'],
  ignorePatterns: ['dist', 'node_modules', 'public'],
  rules: {
    // 开发期允许 console(mock 调试需要)
    'no-console': 'off',
    // any 降级为警告,不阻断构建
    '@typescript-eslint/no-explicit-any': 'warn',
    // 未使用变量降级为警告,下划线开头参数豁免;解构剔字段的 rest 参数豁免
    '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', ignoreRestSiblings: true }],
  },
}