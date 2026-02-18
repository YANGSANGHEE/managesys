<template>
  <div class="placeholder-page">
    <p class="message">{{ pageTitle }} 페이지는 준비 중입니다.</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pageTitle = computed(() => route.meta?.title || '이')
</script>

<style scoped>
.placeholder-page {
  padding: 2rem;
  text-align: center;
  color: #666;
}
.message {
  font-size: 1.1rem;
}
</style>
