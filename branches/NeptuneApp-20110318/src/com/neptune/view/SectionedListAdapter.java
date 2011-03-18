/*
 * Copyright (c) 2010, University of Victoria
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    
 *    This product includes software developed by the University of Victoria.
 * 
 * 4. Neither the name of the University of Victoria nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE University of Victoria ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE University of Victoria BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.neptune.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.neptune.R;

/**
 * This is a special adapter that takes several 'sub lists', provides them with headers or
 * sections, and then combines them together to appear as one ListView.
 * In this application, the SectionedAdapter has each section as a location (eg Folger), and is given a
 * cursor which is then hooked up to a DeviceListAdapter to display the device information
 * at each location.
 */
public class SectionedListAdapter extends BaseAdapter{
	
	private List<Section> sections=new ArrayList<Section>();
	private static int TYPE_SECTION_HEADER = 0;

	private Context context;
	
	/**
	 * Constructor of SectionedListAdapter.
	 * 
	 * @param context
	 */
	public SectionedListAdapter(Context context) {
		this.context = context; 
	}

	/**
	 * Add section by giving a caption and adapter for the list of this section.
	 * 
	 * @param caption Header caption of the section.
	 * @param adapter Adapter of device list of given section.
	 */
	public void addSection(String caption, Adapter adapter) {
		sections.add(new Section(caption, adapter));
	}
	
	/**
	 * Get total number of items including all the header and devices.
	 */
	public int getCount() {
		int total=0;

		for (Section section : this.sections) {
			total += section.adapter.getCount() + 1; // add one for header
		}
		return total;
	}
	
	/**
	 * Get the number of ViewType. 
	 * 
	 * The value is the header's ViewType plus the total ViewType of each section.
	 */
	public int getViewTypeCount() {
		int total=1;	// one for the header, plus those from sections

		for (Section section : this.sections) {
			total += section.adapter.getViewTypeCount();
		}
		return total;
	}
	
	/**
	 * Return the selected item's ViewType.
	 */
	public int getItemViewType(int position) {
		int typeOffset = TYPE_SECTION_HEADER + 1;	// start counting from here

		for (Section section : this.sections) {
			if (position==0) {
				return(TYPE_SECTION_HEADER);
			}
			
			int size=section.adapter.getCount()+1;
			if (position<size) {
				return(typeOffset+section.adapter.getItemViewType(position-1));
			}
			position-=size;
			typeOffset += section.adapter.getViewTypeCount();
		}
		return -1;
	}

	/**
	 * @return false as header of each section is not selectable.
	 */
	public boolean areAllItemsSelectable() {
		return false;
	}

	/**
	 * Disable the item which is of type header
	 */
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_SECTION_HEADER;
	}

	/**
	 * Construct a view to hold each row. 
	 * 
	 * If the row is the start of the section, construct a header view.
	 * 
	 * If the row is the details of the section, construct a device view through the DeviceListAdapter.
	 * 
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionIndex=0;

		for (Section section : this.sections) {
			if (position==0) {
				return getHeaderView(section.caption, sectionIndex,convertView, parent);
			}

			int size=section.adapter.getCount()+1;
			if (position<size) {
				return section.adapter.getView(position-1,convertView,parent);
			}
			position-=size;
			sectionIndex++;
		}
		return null;
	}
	
	/**
	 * Construct a header view.
	 * 
	 * @param caption Caption to be displayed in the header.
	 * @param index
	 * @param convertView
	 * @param parent
	 * @return
	 */
	private View getHeaderView(String caption, int index, View convertView, ViewGroup parent) {
		TextView result = (TextView)convertView;
		if (convertView == null) {
			result = (TextView)LayoutInflater.from(context).inflate(R.layout.header, null);
		}
		result.setText(caption);
		return result;
	}
	
	/**
	 * Get the value of the selected item of a given position.
	 * 
	 * If the selected item is a header, return null.
	 * 
	 * If the selected item is a device, return device's cursor through the DeviceListAdapter.
	 * 
	 */
	public Cursor getItem(int position) {
		for (Section section : this.sections) {
			if (position == 0) {
				return null;
			}
			int size = section.adapter.getCount() + 1;
			if (position < size) {
				return section.adapter.getItem(position - 1);
			}
			position -= size;
		}
		return null;
	}
	
	/**
	 * Get the ID of the selected item of a given position.
	 * 
	 * If the selected item is a header, return the position value.
	 * 
	 * If the selected item is a device, return the device ID through DeviceListAdapter.
	 * 
	 */
	public long getItemId(int position) {
		for(Section section: this.sections){
			if(position == 0){
				return position;
			}
			int size = section.adapter.getCount() + 1;
			if(position<size){
				return section.adapter.getItemId(position - 1);
			}
			position -= size;
		}
		return -1;
	}
	
	/**
	 * Helper function to change the cursor. Used to switch between favourites and all.
	 * 
	 * @param cursor
	 */
	public void updateCursors(Cursor[] c){
		int i = 0;
		for(Section section : this.sections){
			section.adapter.updateCursor(c[i]);
			i++;
		}
	}
	
	/**
	 * A helper class to hold the caption of the header (which is the location) and
	 * the adapter for the data that belongs to a single section.
	 */
	class Section {
		String caption;
		DeviceListAdapter adapter;

		Section(String caption, Adapter adapter) {
			this.caption=caption;
			this.adapter=(DeviceListAdapter) adapter;
		}
	}
}