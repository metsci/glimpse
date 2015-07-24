package com.metsci.glimpse.axis.tagged;

import java.util.List;
import java.util.Map;

public class OrderedConstraint extends NamedConstraint {

	private List<String> constraintIds;
	private double buffer = 1.0;
	
	public OrderedConstraint(String name, List<String> constraints) {
		super(name);
		constraintIds = constraints;
		// TODO Auto-generated constructor stub
	}
	
	public OrderedConstraint(String name, double buffer, List<String> constraints){
		super(name);
		this.buffer = buffer; 
		constraintIds = constraints;
	}

	@Override
	public void applyConstraint(TaggedAxis1D currentAxis,
			Map<String, Tag> previousTags) {
		// TODO Auto-generated method stub
		String tagIndex = constraintIds.get(0);
		for(int k = constraintIds.size()-2; k >= 0; k--)
		{
			String temp = constraintIds.get(k);
			if(previousTags.get(temp).getValue() < currentAxis.getTag(temp).getValue())
				tagIndex = temp;
		}
		for(int k = 1; k < constraintIds.size(); k++)
		{
			String temp = constraintIds.get(k);
			if(previousTags.get(temp).getValue() > currentAxis.getTag(temp).getValue())
				tagIndex = temp;
		}
		double newVal = currentAxis.getTag(tagIndex).getValue();
		if(newVal > previousTags.get(tagIndex).getValue())
		{
			for(int k = 0; k < constraintIds.size()-1; k++)
			{
				if(currentAxis.getTag(constraintIds.get(k)).getValue() > currentAxis.getTag(constraintIds.get(k+1)).getValue() - buffer)
					currentAxis.getTag(constraintIds.get(k+1)).setValue(currentAxis.getTag(constraintIds.get(k)).getValue() + buffer);
			}
		}
		else if(newVal < previousTags.get(tagIndex).getValue())
		{
			for(int k = constraintIds.size()-1; k > 0; k--)
			{
				if(currentAxis.getTag(constraintIds.get(k-1)).getValue() > currentAxis.getTag(constraintIds.get(k)).getValue() - buffer)
					currentAxis.getTag(constraintIds.get(k-1)).setValue(currentAxis.getTag(constraintIds.get(k)).getValue() - buffer);
			}
		}
	}

}
