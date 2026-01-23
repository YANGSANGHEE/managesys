<template>
  <header class="content-header">
    <h1>{{ currentMenu }}</h1>
    <div class="flex justify-between items-center">
      <div class="user-info">
        <strong style="padding-right: 10px">더원컴퍼니님</strong>
      </div>
      <button class="logout-btn">로그아웃</button>
    </div>
  </header>
</template>
<script setup>

</script>

<style scoped>
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.content-header h1 {
  font-size: 1.8rem;
  color: #444;
}


/*로그아웃*/
.logout-btn {
  background: transparent;
  border: none;
  color: #6b7280;       /* gray-500 느낌 */
  font-size: 13px;
  padding: 4px 6px;
  border-radius: 6px;
  cursor: pointer;
}

.logout-btn:hover {
  color: #111827;
  background-color: #f3f4f6;
}
</style>