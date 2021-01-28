package io.cucumber.cucumberexpressions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

//this is copied  from cucumber to get access to additional data
final class GroupBuilder_ {
	private final List<GroupBuilder_> groupBuilders = new ArrayList<>();
	private final boolean capturing;
    private String source;
	private int startIndex;
	private int endIndex;

	public GroupBuilder_(int startIndex, boolean capturing) {
		this.startIndex = startIndex;
		this.capturing = capturing;
	}

	/**
	 * 
	 * @return the index of the group in the original expression string
	 */
	public int getStartIndex() {
		return startIndex;
	}

	void add(GroupBuilder_ groupBuilder) {
        groupBuilders.add(groupBuilder);
    }

    Group build(Matcher matcher, Iterator<Integer> groupIndices) {
        int groupIndex = groupIndices.next();
        List<Group> children = new ArrayList<>(groupBuilders.size());
		for (GroupBuilder_ childGroupBuilder : groupBuilders) {
            children.add(childGroupBuilder.build(matcher, groupIndices));
        }
        return new Group(matcher.group(groupIndex), matcher.start(groupIndex), matcher.end(groupIndex), children);
    }

    boolean isCapturing() {
        return capturing;
    }

	public void moveChildrenTo(GroupBuilder_ groupBuilder) {
		for (GroupBuilder_ child : groupBuilders) {
            groupBuilder.add(child);
        }
    }

	public List<GroupBuilder_> getChildren() {
        return groupBuilders;
    }

    public String getSource() {
        return source;
    }

    void setSource(String source) {
        this.source = source;
    }

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}
}
