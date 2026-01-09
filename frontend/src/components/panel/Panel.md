# Panel Component

The `Panel` component is a flexible container for grouping content with optional styling, a title, and a tag.
It provides a consistent layout structure for different sections of a page.

## Props

| Prop Name     | Type      | Required  | Default  | Description                                                                                     |
|---------------|-----------|-----------|----------|-------------------------------------------------------------------------------------------------|
| `title`       | string    | No        | `null`   | The title displayed at the top of the panel. Rendered as a `Title` component with `h3` variant. |
| `customClass` | string    | No        | `null`   | Additional custom CSS class applied to the root `.panel` element.                               |
| `tag`         | string    | No        | `null`   | Optional tag displayed above the title. Useful for labels, years, or categories.                |

## Slots

| Slot Name  | Description                                                 |
|------------|-------------------------------------------------------------|
| *default*  | Main content of the panel. Appears below the title and tag. |

## Structure

```vue
<template>
  <div class="panel" :class="props.customClass">
    <div v-if="props.tag" class="panel-tag">
      {{ props.tag }}
    </div>
    <Title v-if="props.title" variant="h3" weight="bold" color="black" class="panel-title">{{ props.title }}</Title>
    <slot />
  </div>
</template>
```

- The `.panel` container wraps all panel content.
- If `tag` is provided, it is displayed inside `.panel-tag` above the title.
- If `title` is provided, it is rendered as a `Title` component (`h3`, bold, black).
- All slot content is rendered below the title.

## Example Usage

```vue
<Panel
  tag="2025"
  title="My Scope 1 and 2 Data"
  customClass="highlighted-panel"
>
  <p>This is the main panel content. You can insert any custom components here.</p>
</Panel>
```

## Styling

The component imports its styles from:

```css
@import "@/styles/panel.css";
```

You can extend or override styles by passing a `customClass` prop and defining your own CSS rules.

---
**Best Practices:**
- Use `tag` for small labels like dates or categories.
- Use `customClass` to differentiate panel styles across contexts without modifying the base CSS.
