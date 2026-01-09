<template>
  <Teleport to="body">
    <section v-if="open" class="modal-background" @keydown.esc="onEsc">
      <div class="modal" :class="size === 'xl' && 'modal--xl'" role="dialog" aria-modal="true" :aria-labelledby="modalTitle ? 'modal-title' : undefined">
        <div class="modal-content">

          <div class="close-icon">
          <Button variant="icon" icon icon-position="right" @click="closeWithNoAction">
              <template #icon>
                <IconClose />
              </template>
          </Button>
          </div>
          <slot v-if="customIcon" name="icon" class="icon" />

          <IconConfirmed v-if="variant === 'confirm'" class="icon"/>
          <IconAlert v-if="variant === 'alert'" class="icon icon--alert"/>
          <IconError v-if="variant === 'error'" class="icon icon--error"/>

          <div class="panel-head">
            <div class="title-heading-4">{{modalTitle}}</div>
          </div>

          

          <div class="modal-body">
            <slot />
          </div>
          <div class="modal-footer">
            <Button
                v-if="ctaCloseLabel"
                variant="secondary"
                @click="emitClose"
                :aria-label="ctaCloseLabel">
              {{ ctaCloseLabel }}
            </Button>

            <div class="flex justify-end gap-2">
              <div v-if="textSideButton" class="modal-right-button-info-text">{{ textSideButton }}</div>
              <Button
                  v-if="ctaConfirmLabel"
                  :variant="(variant === 'alert' || variant === 'error') ? 'alert' : 'primary'"
                  @click="emitConfirm"
                  :aria-label="ctaConfirmLabel"
                  :disabled="disableRightButton">
                {{ ctaConfirmLabel }}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </section>
  </Teleport>
</template>

<script setup lang="ts">
import Button from "@/components/button/Button.vue"
import IconConfirmed from "@/components/icons/IconConfirmed.vue"
import IconAlert from "@/components/icons/IconAlert.vue"
import IconError from "@/components/icons/IconError.vue"
import type {VNode} from "vue";
import IconClose from "../icons/IconClose.vue";

const open = defineModel<boolean>('open', { default: false }) // v-model:open

const props = withDefaults(defineProps<{
  variant?: 'confirm' | 'alert' | 'error' | 'simple'
  modalTitle?: string
  ctaCloseLabel?: string
  ctaConfirmLabel?: string
  showFooter?: boolean
  closeOnEsc?: boolean
  customIcon?: VNode
  size?: 'xl'
  textSideButton?: string
  disableRightButton?: boolean
}>(), {
  variant: 'confirm',
  showFooter: true,
  closeOnEsc: true,
})

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'close'): void
}>()

function emitConfirm() {
  open.value = false
  emit('confirm')
}

function emitClose() {
  open.value = false
  emit('close')
}

function closeWithNoAction () {
  open.value = false
}

function onEsc() {
  if (props.closeOnEsc) emitClose()
}
</script>

<style scoped>
@import "@/styles/modal.css";
</style>
