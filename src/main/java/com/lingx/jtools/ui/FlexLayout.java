package com.lingx.jtools.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

/**
 * 12-column row layout with optional flex, wrap, grow, and fixed width/height hints in the constraint string.
 */
public class FlexLayout implements LayoutManager {
	private final Map<Component, String> map = new HashMap<>();
	private int rowsHeight = -1;
	private int cellSpacing = 0;
	private int margin = 0;

	public FlexLayout() {
	}

	public FlexLayout(int rowsHeight, int cellSpacing, int margin) {
		this.rowsHeight = rowsHeight;
		this.cellSpacing = cellSpacing;
		this.margin = margin;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		map.put(comp, name);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		map.remove(comp);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return layoutImpl(parent, false, true);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return layoutImpl(parent, false, false);
	}

	@Override
	public void layoutContainer(Container parent) {
		layoutImpl(parent, true, true);
	}

	/**
	 * @param apply      when true, calls {@link Component#setBounds}
	 * @param useMinimum when false, use {@link Component#getMinimumSize} for non-fixed row height hints
	 */
	private Dimension layoutImpl(Container parent, boolean apply, boolean useMinimum) {
		Insets insets = parent.getInsets();
		int inner = parent.getWidth() - insets.left - insets.right;
		if (inner <= 0) {
			inner = 720;
		}

		double maxWidth = inner;
		double colWidth = (maxWidth - cellSpacing * 11 - 2 * margin) / 12.0;
		if (colWidth < 0) {
			colWidth = 0;
		}

		int i = 0;
		double widthDifference = 0;
		int top = insets.top + margin;
		int extentRight = insets.left + margin;
		int extentBottom = top;

		for (Component comp : parent.getComponents()) {
			int height = defaultRowHeight(parent, insets, useMinimum, comp);
			int flex = getFlex(comp);
			int wrap = 0;
			boolean isgrow = isGrow(comp);
			boolean iswrap = isWrap(comp);
			if (isgrow) {
				flex = 12 - (i % 12);
			} else if (iswrap) {
				wrap = 12 - (i % 12) - flex;
			}

			double x = insets.left + (colWidth + cellSpacing) * (i % 12) + margin - widthDifference;
			int height2 = getHeight(comp);
			if (height2 > 0) {
				height = height2;
			}

			double width1 = colWidth * flex + (cellSpacing * (flex - 1)) + widthDifference;
			int width2 = getWidth(comp);
			double w;
			if (width2 == -1) {
				w = width1;
				widthDifference = 0;
			} else {
				w = width2;
				widthDifference = width1 - width2;
			}
			i = i + flex + wrap;

			int iw = (int) Math.round(w);
			if (iw < 0) {
				iw = 0;
			}
			int ix = (int) Math.round(x);
			if (apply) {
				comp.setBounds(ix, top, iw, height);
			}
			extentRight = Math.max(extentRight, ix + iw);
			extentBottom = Math.max(extentBottom, top + height);

			if (isgrow || iswrap || i % 12 == 0) {
				widthDifference = 0;
				top += height + cellSpacing;
			}
		}

		int wOut = extentRight + margin + insets.right;
		int hOut = extentBottom + margin + insets.bottom;
		return new Dimension(Math.max(wOut, insets.left + insets.right + 2 * margin),
				Math.max(hOut, insets.top + insets.bottom + 2 * margin));
	}

	private int defaultRowHeight(Container parent, Insets insets, boolean useMinimum, Component comp) {
		if (rowsHeight == -1) {
			int ph = parent.getHeight() - insets.top - insets.bottom - 2 * margin;
			return Math.max(1, ph);
		}
		if (rowsHeight > 0) {
			return rowsHeight;
		}
		Dimension d = useMinimum ? comp.getMinimumSize() : comp.getPreferredSize();
		return Math.max(1, d.height);
	}

	private int getWidth(Component comp) {
		try {
			String temp = map.get(comp);
			if (temp != null && temp.contains("width")) {
				for (String str : temp.split(";")) {
					if (str.contains("width")) {
						return Integer.parseInt(str.split(":")[1].replace("px", "").trim());
					}
				}
			}
		} catch (Exception ignored) {
			// malformed constraint; fall through
		}
		return -1;
	}

	private int getHeight(Component comp) {
		try {
			String temp = map.get(comp);
			if (temp != null && temp.contains("height")) {
				for (String str : temp.split(";")) {
					if (str.contains("height")) {
						return Integer.parseInt(str.split(":")[1].replace("px", "").trim());
					}
				}
			}
		} catch (Exception ignored) {
			// malformed constraint; fall through
		}
		return -1;
	}

	private int getFlex(Component comp) {
		try {
			String temp = map.get(comp);
			if (temp != null && temp.contains("flex")) {
				for (String str : temp.split(";")) {
					if (str.contains("flex")) {
						return Integer.parseInt(str.split(":")[1].trim());
					}
				}
			}
		} catch (Exception ignored) {
			// malformed constraint; fall through
		}
		return 1;
	}

	private boolean isGrow(Component comp) {
		String temp = map.get(comp);
		return temp != null && temp.contains("grow");
	}

	private boolean isWrap(Component comp) {
		String temp = map.get(comp);
		return temp != null && temp.contains("wrap");
	}

	public int getRowsHeight() {
		return rowsHeight;
	}

	public void setRowsHeight(int rowsHeight) {
		this.rowsHeight = rowsHeight;
	}

	public int getCellSpacing() {
		return cellSpacing;
	}

	public void setCellSpacing(int cellSpacing) {
		this.cellSpacing = cellSpacing;
	}

	public int getMargin() {
		return margin;
	}

	public void setMargin(int margin) {
		this.margin = margin;
	}
}
