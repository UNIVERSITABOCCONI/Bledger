<template>
  <footer class="app-footer">
    <p class="footer-copy">{{ copyLabel }}</p>

    <!--<ul class="footer-social-links">
      <li>
        <a :class="'footer-social-item-link'" href="https://www.youtube.com/">
          <IconYoutube />
        </a>
      </li>
      <li>
        <a class="footer-social-item-link" href="https://www.facebook.com/">
          <IconFacebook />
        </a>
      </li>
      <li>
        <a class="footer-social-item-link" href="https://www.twitter.com/">
          <IconTwitter />
        </a>
      </li>
      <li>
        <a class="footer-social-item-link" href="https://www.instagram.com/">
          <IconInstagram />
        </a>
      </li>
      <li>
        <a class="footer-social-item-link" href="https://www.linkedin.com/">
          <IconLinkedin />
        </a>
      </li>
    </ul>-->
  </footer>
</template>

<script setup lang="ts">

const props = withDefaults(defineProps<{
  copyLabel: string;
}>(), {
  copyLabel: 'B-Ledger @ 2025. All rights reserved.'
})
</script>

<style scoped>
@import "@/styles/footer.css";
</style>