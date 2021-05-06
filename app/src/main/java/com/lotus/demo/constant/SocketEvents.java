package com.lotus.demo.constant;

public class SocketEvents {
    /* #region Socket.io events  */
    public static final String CONNECT = "connect";
    public static final String CONNECT_ERROR = "connect_error";
    /* #endregion */

    /* #region Custom events */
    public static final String UPDATE_USER_LIST = "UPDATE_USER_LIST";
    public static final String CALL_MAKE = "CALL_MAKE";
    public static final String CALL_ICE_CANDIDATE = "CALL_ICE_CANDIDATE";
    public static final String CALL_REJECT = "CALL_REJECT";
    public static final String CALL_CANCEL = "CALL_CANCEL";
    public static final String CALL_ACCEPT = "CALL_ACCEPT";
    public static final String CALL_OFFER = "CALL_OFFER";
    public static final String CALL_ANSWER = "CALL_ANSWER";
    public static final String CALL_DISCONNECT = "CALL_DISCONNECT";
    public static final String CALL_FINISH = "CALL_FINISH";
    /* #endregion */
}
