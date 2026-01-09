<template>
  <div class="suppliers-table">
    <!-- Header -->
    <ul class="table-header">
      <li v-for="header in headers" :key="header.key" :style="{ flexBasis: header.width }">
        {{ header.name }}
      </li>
    </ul>

    <!-- Rows -->
    <div :class="{ fixedHeight }">
      <div v-if="isEmpty" class="empty-state">No elements in table</div>
      <ul v-else v-for="row in rows" :key="row.id ?? row.name" class="table-row" :class="{ clickable: !!row.onClick }"
        @click="row.onClick?.()">
        <li v-for="header in headers" :key="`${(row.id ?? row.name)}-${header.key}`"
          :style="{ flexBasis: header.width }" class="table-cell">
          <!-- NAME -->
          <template v-if="header.type === 'name' && showImage">
            <div class="supplier-info">
              <div class="avatar-wrapper">
                <Image class="avatar-img" :src="row.avatar || '/company-placeholder.png'" alt="{{ row.name }}" />
              </div>
              <span class="company-name">{{ row.name ?? '-' }}</span>
            </div>
          </template>

          <!-- LEVEL -->
          <template v-else-if="header.type === 'level'">
            <div class="levels">
              <span class="level-badge">{{ row.level ?? row[header.key] ?? '-' }}</span>
            </div>
          </template>

          <!-- STATUS -->
          <template v-else-if="header.type === 'status'">
            <div class="scope-status">
              <template v-if="row.custom">
                <component :is="row.custom" />
              </template>
              <template v-else-if="row.transactionId">
                <TransactionStatus :transactionId="row.transactionId" :verify="row.verify" :text="row.verify ? 'Verified' : 'Uploaded'" />
              </template>
              <template v-else-if="row.uploaded">
                <TransactionStatus status="CONFIRMED" :verify="row.verified" :text="row.verified ? 'Verified' : 'Uploaded'" />
              </template>
              <template v-else>
                <span class="dashes">-------</span>
              </template>
            </div>
          </template>

          <!-- VALUES -->
          <template v-else-if="header.type === 'values'">
            <div class="values-display">
              <span class="value-item">
                <strong>q:</strong> {{ row.quantity != null ? row.quantity : '-' }}
              </span>
              <span class="value-item">
                <strong>t:</strong> {{ row.transportationEmission != null ? row.transportationEmission : '-' }}
              </span>
            </div>
          </template>

          <!-- INPUT -->
          <template v-else-if="header.type === 'input'">
            <input
              type="text"
              :value="row[header.key]"
              @input="handleInputChange(row.id, header.key, $event)"
              class="table-input"
            />
          </template>


          <!-- CUSTOM GENERIC -->
          <template v-else-if="isVNode(row[header.key])">
            <component :is="row[header.key]" />
          </template>

          <!-- FALLBACK -->
          <template v-else>
            <span>{{ row[header.key] ?? '-' }}</span>
          </template>
        </li>
      </ul>
    </div>

    <!-- Pagination -->
    <div class="pagination" v-if="!isEmpty && showPagination">
      <Button variant="secondary" :disabled="disablePrev" @click="handlePrev"><IconChevronLeft class="pagination-icon"/></Button>
      <span>{{ displayPage }}/{{ totalPages }}</span>
      <Button variant="secondary" :disabled="disableNext" @click="handleNext"><IconChevronRight class="pagination-icon"/></Button>
      <div class="page-size">
        <label>Rows per page:</label>
        <select :value="size" @change="handleSizeChange">
          <option v-for="opt in pageSizeOptionsInternal" :key="opt" :value="opt">{{ opt }}</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { isVNode, VNode, computed } from "vue";
import Image from "@/components/image/Image.vue";
import TransactionStatus from "@/components/TransactionStatus.vue";
import Button from "@/components/button/Button.vue";
import IconChevronLeft from "../icons/IconChevronLeft.vue";
import IconChevronRight from "../icons/IconChevronRight.vue";

const emit = defineEmits<{
  inputChange: [rowId: string, key: string, value: string]
}>()

interface Row {
  id?: string | number;
  avatar?: string;
  name?: string;
  level?: number;
  uploaded?: boolean;
  verified?: boolean;
  custom?: VNode | VNode[];
  verifiedDate?: string;
  transactionId?: string;
  verify?: boolean;
  onClick?: () => void;
  quantity?: number;
  transportationEmission?: number;
  [key: string]: unknown;
}

interface HeaderItem {
  key: string; // unique column key
  name: string; // header label
  width: string; // e.g. '55%'
  type: "name" | "level" | "status" | string; // rendering type
}

type FetchFn = (opts: { page: number; size?: number }) => Promise<unknown> | unknown;

const props = withDefaults(
  defineProps<{
    rows: Row[];
    headers: HeaderItem[];
    fixedHeight?: boolean;
    showImage?: boolean;
    showPagination?: boolean;
    total?: number;
    page?: number; // 0-based
    size?: number;
    fetchFn?: FetchFn;
    pageSizeOptions?: number[];
    loading?: boolean;
  }>(),
  {
    showImage: true,
    showPagination: true,
    total: 0,
    page: 0,
    size: 10,
    pageSizeOptions: () => [10, 20, 50],
    loading: false,
  }
);

const totalPages = computed(() => Math.max(1, Math.ceil((props.total ?? 0) / (props.size || 10))));
const displayPage = computed(() => (props.page ?? 0) + 1);
const disablePrev = computed(() => !!props.loading || (props.page ?? 0) <= 0);
const disableNext = computed(() => !!props.loading || ((props.page ?? 0) + 1) >= totalPages.value);
const pageSizeOptionsInternal = computed(() => props.pageSizeOptions ?? [10, 20, 50]);
const isEmpty = computed(() => ((props.total ?? 0) === 0) || ((props.rows?.length ?? 0) === 0));

function handlePrev() {
  if (!props.fetchFn) return;
  const nextPage = Math.max(0, (props.page ?? 0) - 1);
  props.fetchFn({ page: nextPage, size: props.size });
}

function handleNext() {
  if (!props.fetchFn) return;
  const nextPage = Math.min((props.page ?? 0) + 1, totalPages.value - 1);
  props.fetchFn({ page: nextPage, size: props.size });
}

function handleSizeChange(e: Event) {
  if (!props.fetchFn) return;
  const target = e.target as HTMLSelectElement;
  const newSize = Number(target.value);
  props.fetchFn({ page: 0, size: newSize });
}

function handleInputChange(rowId: string | number, key: string, event: Event) {
  const target = event.target as HTMLInputElement;
  emit('inputChange', rowId.toString(), key, target.value);
}
</script>

<style scoped>
@import "@/styles/tableCustom.css";

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 64px;
  color: #666;
  font-style: italic;
  padding: 12px;
}
</style>
