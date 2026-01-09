# Title Component 

The `Title` component is a reusable, customizable title element that allows you to control its variant, font weight, color, and apply additional custom CSS classes.  
It is primarily used for headings and display titles in the UI, ensuring consistent typography styles across the application.

## Props

| Prop Name    | Type                                                          | Required  | Default  | Description                                                       |
|--------------|---------------------------------------------------------------|-----------|----------|-------------------------------------------------------------------|
| `variant`    | `'display'` \| `'h1'` \| `'h2'` \| `'h3'` \| `'h4'` \| `'h5'` | ✅         | —        | Determines the text size and semantic style of the title.         |
| `weight`     | `'regular'` \| `'medium'` \| `'semi-bold'` \| `'bold'`        | ✅         | —        | Sets the font-weight of the title.                                |
| `color`      | `'black'` \| `'blue'` \| `'white'` \| `'dark-blue'`           | ✅         | —        | Defines the text color variant.                                   |
| `extraClass` | `string`                                                      | ❌         | —        | Additional custom CSS classes to be applied to the title element. |

## Slots
| Name     | Description |
|----------|-------------|
| default  | The content of the title (usually text or inline HTML). |

## Computed Classes
The component dynamically generates CSS classes based on the given props:
- `variantClass` → `variant-{variant}` (e.g., `variant-h1`)
- `weightClass` → `weight-{weight}` (e.g., `weight-bold`)
- `colorClass` → `color-{color}` (e.g., `color-blue`)

These classes are then combined with `extraClass` (if provided).

## Example Usage

```vue
<template>
  <Title variant="h1" weight="bold" color="blue">
    Welcome to the Dashboard
  </Title>

  <Title variant="h3" weight="medium" color="dark-blue" extraClass="custom-spacing">
    Section Title
  </Title>
</template>
```

This would render:
```html
<div class="title variant-h1 weight-bold color-blue">
  Welcome to the Dashboard
</div>
<div class="title variant-h3 weight-medium color-dark-blue custom-spacing">
  Section Title
</div>
```

## Styling
The component relies on the `title.css` stylesheet for typography styles.  
Make sure the relevant CSS classes for `variant-*`, `weight-*`, and `color-*` are defined in `@/styles/title.css`.

