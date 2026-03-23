package com.sys.managesys.common.controller;

import com.sys.managesys.common.dto.CommonCodeDto;
import com.sys.managesys.common.dto.GroupCodeDto;
import com.sys.managesys.common.mapper.CommonCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 API (TB_GROUP_CODE / TB_COMMON_CODE)
 */
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CommonCodeMapper commonCodeMapper;

    // ─── 드롭다운용 (기존) ───────────────────────────────────────────────────
    @PostMapping("/list")
    public List<CommonCodeDto> list(@RequestBody Map<String, Object> body) {
        if (body == null || body.isEmpty()) return List.of();
        Object raw = body.get("groupCode");
        if (raw == null) raw = body.get("group_code");
        if (raw == null) return List.of();
        String groupCode = raw.toString().trim();
        if (groupCode.isEmpty()) return List.of();
        return commonCodeMapper.selectByGroupCode(groupCode);
    }

    // ─── 그룹코드 CRUD ───────────────────────────────────────────────────────
    @PostMapping("/groups/list")
    public List<GroupCodeDto> groupList(@RequestBody(required = false) Map<String, Object> body) {
        String groupName = (body != null && body.get("groupName") != null)
                ? body.get("groupName").toString() : null;
        return commonCodeMapper.selectAllGroups(groupName);
    }

    @PostMapping("/groups/register")
    public ResponseEntity<?> groupRegister(@RequestBody GroupCodeDto dto) {
        if (dto.getGroupCode() == null || dto.getGroupCode().isBlank())
            return ResponseEntity.badRequest().body("그룹코드를 입력해주세요.");
        if (dto.getGroupName() == null || dto.getGroupName().isBlank())
            return ResponseEntity.badRequest().body("그룹명을 입력해주세요.");
        commonCodeMapper.insertGroupCode(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/modify")
    public ResponseEntity<?> groupModify(@RequestBody GroupCodeDto dto) {
        if (dto.getGroupCode() == null || dto.getGroupCode().isBlank())
            return ResponseEntity.badRequest().body("그룹코드를 입력해주세요.");
        commonCodeMapper.updateGroupCode(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/groups/remove")
    public ResponseEntity<?> groupRemove(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("groupCode") == null)
            return ResponseEntity.badRequest().body("그룹코드를 입력해주세요.");
        String groupCode = body.get("groupCode").toString();
        int cnt = commonCodeMapper.countCodesByGroupCode(groupCode);
        if (cnt > 0)
            return ResponseEntity.badRequest().body("해당 그룹에 상세코드가 존재합니다. 상세코드를 먼저 삭제해주세요.");
        commonCodeMapper.deleteGroupCode(groupCode);
        return ResponseEntity.ok().build();
    }

    // ─── 상세코드 CRUD ───────────────────────────────────────────────────────
    @PostMapping("/detail/list")
    public List<CommonCodeDto> detailList(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("groupCode") == null) return List.of();
        String groupCode = body.get("groupCode").toString();
        return commonCodeMapper.selectAllByGroupCode(groupCode);
    }

    @PostMapping("/detail/register")
    public ResponseEntity<?> detailRegister(@RequestBody CommonCodeDto dto) {
        if (dto.getGroupCode() == null || dto.getGroupCode().isBlank())
            return ResponseEntity.badRequest().body("그룹코드를 입력해주세요.");
        if (dto.getCodeValue() == null || dto.getCodeValue().isBlank())
            return ResponseEntity.badRequest().body("코드값을 입력해주세요.");
        if (dto.getCodeName() == null || dto.getCodeName().isBlank())
            return ResponseEntity.badRequest().body("코드명을 입력해주세요.");
        commonCodeMapper.insertCode(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detail/modify")
    public ResponseEntity<?> detailModify(@RequestBody CommonCodeDto dto) {
        commonCodeMapper.updateCode(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detail/remove")
    public ResponseEntity<?> detailRemove(@RequestBody Map<String, Object> body) {
        if (body == null || body.get("groupCode") == null || body.get("codeValue") == null)
            return ResponseEntity.badRequest().body("그룹코드와 코드값을 입력해주세요.");
        String groupCode = body.get("groupCode").toString();
        String codeValue = body.get("codeValue").toString();
        commonCodeMapper.deleteCode(groupCode, codeValue);
        return ResponseEntity.ok().build();
    }
}
