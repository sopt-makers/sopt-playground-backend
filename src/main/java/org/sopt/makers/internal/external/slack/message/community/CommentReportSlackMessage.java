package org.sopt.makers.internal.external.slack.message.community;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.external.slack.SlackMessageUtil;
import org.sopt.makers.internal.external.slack.message.SlackMessageBuilder;

import static org.sopt.makers.internal.external.slack.message.community.CommunitySlackConstants.*;

@RequiredArgsConstructor
public class CommentReportSlackMessage implements SlackMessageBuilder {

    private final SlackMessageUtil slackMessageUtil;
    private final Long postId;
    private final String reporterName;
    private final String commentContent;

    @Override
    public JsonNode buildMessage() {
        ObjectNode rootNode = slackMessageUtil.getObjectNode();
        rootNode.put("text", COMMENT_REPORT_TITLE);

        ArrayNode blocks = slackMessageUtil.getArrayNode();
        ObjectNode textField = slackMessageUtil.createTextField(COMMENT_REPORT_HEADER);
        ObjectNode contentNode = slackMessageUtil.createSection();

        ArrayNode fields = slackMessageUtil.getArrayNode();
        fields.add(slackMessageUtil.createTextFieldNode(COMMENT_REPORT_REPORTER_LABEL + reporterName));
        fields.add(slackMessageUtil.createTextFieldNode(COMMENT_REPORT_CONTENT_LABEL + commentContent));
        fields.add(slackMessageUtil.createTextFieldNode(COMMENT_REPORT_LINK_LABEL + String.format(COMMENT_REPORT_LINK_FORMAT, postId)));
        contentNode.set("fields", fields);

        blocks.add(textField);
        blocks.add(contentNode);
        rootNode.set("blocks", blocks);

        return rootNode;
    }

    public static CommentReportSlackMessage of(
            SlackMessageUtil slackMessageUtil,
            Long postId,
            String reporterName,
            String commentContent
    ) {
        return new CommentReportSlackMessage(slackMessageUtil, postId, reporterName, commentContent);
    }
}
