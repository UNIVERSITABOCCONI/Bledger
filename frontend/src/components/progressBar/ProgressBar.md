# ProgressBar Component

A simple progress bar component that displays a percentage value and an accompanying message.

## Props

| Prop       | Type     | Required | Description                                 |
|------------|----------|----------|---------------------------------------------|
| `progress` | `number` | ✅        | The progress value as a percentage (0–100). |
| `copy`     | `string` | ✅        | Descriptive text rendered below the bar.    |

## Markup Structure
```vue
<template>
  <div class="progress-bar-container">
    <div class="progress-bar-value">{{ props.progress }}%</div>
    <div class="progress-bar">
      <div class="progress-bar-indicator" :style="{ width: props.progress + '%' }"></div>
    </div>
    <p>{{ copy }}</p>
  </div>
</template>
```

## Usage
```vue
<ProgressBar :progress="72" copy="Processing your export…" />
```
---

## Styling
Base styles are loaded from `@/styles/progressBar.css`. The component exposes the following class hooks:
- `.progress-bar-container`
- `.progress-bar-value`
- `.progress-bar`
- `.progress-bar-indicator`

---

## Notes
- Ensure `progress` is clamped between 0 and 100 to avoid layout issues.
- Long `copy` strings will wrap below the bar; keep it concise for best results.
