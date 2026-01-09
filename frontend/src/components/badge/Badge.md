# Badge Component

The `Badge` component displays a small label (badge) and, optionally, a remove button if a `@remove` listener is provided by the parent.

## Props

| Prop    | Type   | Required | Default | Description                          |
|---------|--------|----------|---------|--------------------------------------|
| `label` | string | ✅        | —       | The text displayed inside the badge. |

## Emits

| Event    | Payload | Description                                                                                           |
|----------|---------|-------------------------------------------------------------------------------------------------------|
| `remove` | none    | Emitted when the remove button is clicked. Only rendered if the parent provides a `@remove` listener. |

## Usage

```vue
<template>
  <!-- Without remove action -->
  <Badge label="Read Only" />

  <!-- With remove action -->
  <Badge label="Removable" @remove="handleRemove" />
</template>

<script setup lang="ts">
import Badge from "@/components/badge/Badge.vue"

function handleRemove() {
  console.log("Badge removed")
}
</script>
```

## Behavior

- If the parent does **not** provide a `@remove` listener, the remove button will not be rendered.
- If the parent provides `@remove="handler"`, the button is displayed and will call `handler` when clicked.
