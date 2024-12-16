package org.koreait.file.services;


import lombok.RequiredArgsConstructor;
import org.koreait.file.constants.FileStatus;
import org.koreait.file.entities.FileInfo;
import org.koreait.file.repositories.FileInfoRepository;
import org.koreait.global.exceptions.UnAuthorizedException;
import org.koreait.member.libs.MemberUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class FileDeleteService {
    private final FileInfoService infoService;
    private final FileInfoRepository infoRepository;
    private final MemberUtil memberUtil;

    public FileInfo delete(Long seq) {
        FileInfo item = infoService.get(seq);
        String filePath = item.getFilePath();
//        System.out.println(filePath);
//        0. 파일 소유자만 삭제 가능하게 통제 - 다만 관리자는 가능
        String createdBy = item.getCreateBy();
//        관리자 아니고 && 비로그인이 아닌 로그인회원이 작성한 File 이고
//&& (비로그인 상태이거나 || 해당 File의 작성자(createdBy)가 아닐때)
        if (!memberUtil.isAdmin() && StringUtils.hasText(createdBy)
                && (!memberUtil.isLogin() || !memberUtil.getMember().getEmail().equals(createdBy))) //작성자 일치
            throw new UnAuthorizedException();

//        1. DB에서 정보를 제거
        infoRepository.delete(item);
        infoRepository.flush();

//        2. 파일이 서버에 존재하면 파일도 삭제
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }

//        3. 삭제된 파일 정보를 반환
        return item;
    }

    public List<FileInfo> deletes(String gid, String location) { //gid, location이 있을 경우
        List<FileInfo> items = infoService.getList(gid, location, FileStatus.ALL);
        items.forEach(i -> delete(i.getSeq()));

        return items;
    }

    public List<FileInfo> deletes(String gid) { // gid만 있을 경우
        return deletes(gid, null);
    }
}
