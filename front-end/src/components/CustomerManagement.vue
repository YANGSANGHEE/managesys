<template>
  <div class="whiteBk">
    <table class="customer-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>이름</th>
          <th>이메일</th>
          <th>가입일</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="customer in customers" :key="customer.id">
          <td>{{ customer.id }}</td>
          <td>{{ customer.name }}</td>
          <td>{{ customer.email }}</td>
          <td>{{ customer.signupDate }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'CustomerManagement',
  data() {
    return {
      customers: [
        { id: 'C001', name: '김철수', email: 'cs.kim@example.com', signupDate: '2023-10-01' },
        { id: 'C002', name: '이영희', email: 'yh.lee@example.com', signupDate: '2023-10-02' },
        { id: 'C003', name: '박지성', email: 'js.park@example.com', signupDate: '2023-10-03' },
        { id: 'C004', name: '최유리', email: 'yr.choi@example.com', signupDate: '2023-10-04' }
      ]
    };
  }
}
</script>

<style scoped>
.customer-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
.customer-table th, .customer-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
.customer-table th {
  background-color: #f2f2f2;
}
</style>
