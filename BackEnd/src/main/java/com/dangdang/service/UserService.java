package com.dangdang.service;

import com.dangdang.dto.request.NotificationUpdateRequest;
import com.dangdang.dto.request.UserUpdateRequest;
import com.dangdang.dto.response.UserInfoResponse;
import com.dangdang.entity.ActivityLevel;
import com.dangdang.entity.User;
import com.dangdang.exception.BusinessException;
import com.dangdang.exception.ErrorCode;
import com.dangdang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [각주 FI] 내 정보 조회/수정, 알림 설정을 담당합니다 (카테고리: users).
 *
 * @lastModified 2026-08-21
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** [각주 FJ] GET /api/users/me. */
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Integer userNo) {
        User user = findUser(userNo);
        return toResponse(user);
    }

    /**
     * [각주 FK] PATCH /api/users/me. 부분 수정 — 요청 바디에서 null인 필드는 안 건드립니다
     * (User.updateProfile() 참고). activityLevel은 화면 문구로 들어오므로 코드로 변환한 뒤 저장합니다.
     */
    @Transactional
    public UserInfoResponse updateMyInfo(Integer userNo, UserUpdateRequest request) {
        User user = findUser(userNo);

        Integer activityLevelCode = request.activityLevel() == null
                ? null
                : ActivityLevel.fromRawText(request.activityLevel()).getCode();

        user.updateProfile(request.nickname(), request.gender(), request.birthDate(),
                request.height(), request.weight(), request.hba1c(),
                activityLevelCode, request.targetGlucose());

        return toResponse(user);
    }

    /** [각주 FL] PATCH /api/users/me/notification. */
    @Transactional
    public UserInfoResponse updateNotification(Integer userNo, NotificationUpdateRequest request) {
        User user = findUser(userNo);
        user.updateNotificationEnabled(Boolean.TRUE.equals(request.notificationEnabled()));
        return toResponse(user);
    }

    private User findUser(Integer userNo) {
        return userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserInfoResponse toResponse(User user) {
        String activityLevelText = user.getActivityLevel() == null
                ? null
                : ActivityLevel.fromCode(user.getActivityLevel()).getRawText();

        return new UserInfoResponse(
                user.getNickname(), user.getEmail(), user.getGender(), user.getBirthDate(),
                user.getHeight(), user.getWeight(), user.getHba1c(), activityLevelText,
                user.getTargetGlucose(), user.isNotificationEnabled(), user.getJoinedAt());
    }
}
