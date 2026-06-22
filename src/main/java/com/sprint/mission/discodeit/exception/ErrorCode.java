package com.sprint.mission.discodeit.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // User
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  USER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "중복된 이메일입니다."),
  USER_USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "중복된 이름입니다."),
  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
  USER_PASSWORD_ALREADY_USED(HttpStatus.CONFLICT, "현재 비밀번호와 일치합니다."),

  // Channel
  CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE_DENIED(HttpStatus.BAD_REQUEST, "PRIVATE 채널은 수정할 수 없습니다."),

  // Message
  MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
  MESSAGE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "메시지 내용이 비어있습니다."),

  // BinaryContent
  BINARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "컨텐츠를 찾을 수 없습니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기 초과: 최대 10MB"),
  INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않는 확장자입니다."),

  // ReadStatus
  READ_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 ReadStatus입니다."),
  READ_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "ReadStatus를 찾을 수 없습니다."),

  // UserStatus
  USER_STATUS_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 UserStatus입니다."),
  USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 스테이터스가 없습니다."),

  // storage
  STORAGE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
  STORAGE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다."),
  STORAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

  // 기타
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "유효성 검사 실패"),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
