package test;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

public class TestTag implements Tag
{
	private PageContext pageContext;
	private Tag parentTag;

	@Override
	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().println("TestTag ÄÚÈÝ");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return this.EVAL_PAGE;
	}

	@Override
	public int doStartTag() throws JspException {
		return this.SKIP_BODY;
	}

	@Override
	public Tag getParent() {
		return this.parentTag;
	}

	@Override
	public void release() {

	}

	@Override
	public void setPageContext(PageContext pageContext) {
		this.pageContext=pageContext;
	}

	@Override
	public void setParent(Tag tag) {
		this.parentTag=tag;
	}
}
