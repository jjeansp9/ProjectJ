package kr.jeet.edu.student.model.request;

public class UpdatePushTokenRequest {
    int memberSeq;  //memberseq
    String token;   //token string for updating
    final String osType = "a";
    public UpdatePushTokenRequest(int memberSeq, String token) {
        this.memberSeq = memberSeq;
        this.token = token;
    }
}
