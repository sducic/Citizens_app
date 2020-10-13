package elfak.mosis.zeljko.citzens_app;

public class Friend_req {
    public String request_type;

    public Friend_req() {

    }
    public Friend_req(String request_type) {
        this.request_type = request_type;
    }
    public void setReqType(String request_type) {
        this.request_type=request_type;
    }
    public String getReqType() {
        return this.request_type;
    }
}
