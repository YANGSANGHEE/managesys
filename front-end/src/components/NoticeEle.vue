<template>
  <div class="bento-grid">
    <section v-for="section in sections" :key="section.title" class="bento-card">
      <div class="card-header">
        <h3>{{ section.title }}</h3>
        <button class="more-btn">+</button>
      </div>
      <table class="data-table">
        <thead>
        <tr>
          <th>제목</th>
          <th>등록자</th>
          <th>등록일</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="n in 4" :key="n">
          <td class="empty-cell">내용이 없습니다.</td>
          <td class="empty-cell">-</td>
          <td class="empty-cell">-</td>
        </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
const sections = [
  { title: '사내 공지사항' },
  { title: '협력점 공지사항' },
  { title: '사내 자료실' },
  { title: '협력점 자료실' },
];
</script>

<style scoped>

.bento-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

.bento-card {
  background: white;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.03);
  transition: transform 0.2s ease;
}

.bento-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  font-size: 1.1rem;
  color: #333;
}

.more-btn {
  background: #f0f2f5;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 8px;
  font-size: 0.85rem;
  color: #888;
  border-bottom: 1px solid #eee;
}

.data-table td {
  padding: 14px 8px;
  font-size: 0.9rem;
  border-bottom: 1px solid #f9f9f9;
}

.empty-cell {
  color: #ccc;
  text-align: center;
}
</style>