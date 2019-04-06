package cn.mrcode.cachepdp.eshop.inventory.web.vo;

/**
 * 请求的响应
 * @author Administrator
 *
 */
public class Response {

	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	
	private String status;
	private String message;
	
	public Response() {
		
	}
	
	public Response(String status) {
		this.status = status;
	}
	
	public Response(String status, String message) {
		this.status = status;
		this.message = message;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
