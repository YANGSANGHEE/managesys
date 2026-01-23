<template>
  <div class="flex dashboard-layout">
  <SideBar/>
    <div class="contents-wrap">
      <HeaderEle/>
        <div class="contents">
          <router-view/>
        </div>
      <FooterEle/>
    </div>
  </div>
</template>
<style scoped>
@import "./style/flex.css";
@import "./style/reset.css";
@import "./style/common.css";
</style>
<script>
import SideBar from './components/SideBar.vue'
// import MainPage from "@/components/MainPage.vue"; // 더 이상 직접 사용하지 않으므로 주석 처리 또는 삭제
import FooterEle from "@/components/FooterEle.vue";
import HeaderEle from "@/components/HeaderEle.vue";


export default {
  name: 'App',
  components: {
    HeaderEle : HeaderEle,
    SideBar: SideBar,
    // MainPage:MainPage,
    FooterEle : FooterEle
  }
}
</script>