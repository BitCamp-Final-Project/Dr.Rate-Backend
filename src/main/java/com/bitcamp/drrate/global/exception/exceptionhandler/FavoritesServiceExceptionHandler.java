package com.bitcamp.drrate.global.exception.exceptionhandler;

import com.bitcamp.drrate.global.code.ErrorCode;
import com.bitcamp.drrate.global.exception.GeneralException;

public class FavoritesServiceExceptionHandler extends GeneralException {
  public FavoritesServiceExceptionHandler(ErrorCode errorCode) {
    super(errorCode);
  }
}