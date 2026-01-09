# vSpacer Component

The **vSpacer** component is a simple utility component used to create vertical spacing between elements in a layout.  
It supports different predefined sizes to maintain design consistency.

---

## Props

| Prop   | Type                                   | Default     | Description                                                                                   |
|--------|----------------------------------------|-------------|-----------------------------------------------------------------------------------------------|
| size   | `'xs' \| 'sm' \| 'md' \| 'lg' \| 'xl'` | `undefined` | Determines the height of the vertical space. If not provided, default CSS styling will apply. |

---

## Usage

```vue
<template>
  <div>
    <p>Content above</p>
    <vSpacer size="md" />
    <p>Content below</p>
  </div>
</template>
```

In this example, the **vSpacer** component inserts medium vertical spacing between two paragraphs.

---

## CSS Modifiers
The component applies a base class `vSpacer` and a size modifier class `vSpacer--{size}`.  
These classes should be defined in `vSpace.css` to control the spacing amount.

**Example CSS:**
```css
.vSpacer--xs { height: 4px; }
.vSpacer--sm { height: 8px; }
.vSpacer--md { height: 16px; }
.vSpacer--lg { height: 24px; }
.vSpacer--xl { height: 32px; }
```
