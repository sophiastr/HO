package module.matches;

import core.db.DBManager;
import core.gui.comp.renderer.HODefaultTableCellRenderer;
import core.gui.comp.renderer.TableHeaderRenderer1;
import core.gui.comp.table.TableSorter;
import core.gui.comp.table.ToolTipHeader;
import core.gui.comp.table.UserColumn;
import core.gui.model.MatchesColumnModel;
import core.gui.model.UserColumnController;
import core.model.HOVerwaltung;
import core.model.UserParameter;
import core.util.HOLogger;
import core.util.Helper;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

final class MatchesTable extends JTable {

	private MatchesColumnModel m_clTableModel;
	private TableSorter m_clTableSorter;

	protected MatchesTable(int matchtyp) {
		super();
		initModel(matchtyp, UserParameter.instance().matchLocation);
		setDefaultRenderer(java.lang.Object.class, new HODefaultTableCellRenderer());
		getTableHeader().setDefaultRenderer(new TableHeaderRenderer1(this));
		getTableHeader().setFont(getTableHeader().getFont().deriveFont(Font.BOLD));
		setSelectionBackground(HODefaultTableCellRenderer.SELECTION_BG);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public void saveColumnOrder() {
		final UserColumn[] columns = m_clTableModel.getDisplayedColumns();
		final TableColumnModel tableColumnModel = getColumnModel();
		for (int i = 0; i < columns.length; i++) {
			columns[i].setIndex(convertColumnIndexToView(i));
			columns[i].setPreferredWidth(tableColumnModel.getColumn(convertColumnIndexToView(i))
					.getWidth());
		}
		m_clTableModel.setCurrentValueToColumns(columns);
		DBManager.instance().saveHOColumnModel(m_clTableModel);
	}

	public void refresh(int iMatchType, MatchLocation matchLocation) {
		initModel(iMatchType, matchLocation);
	}

	protected TableSorter getSorter() {
		return m_clTableSorter;
	}

	protected void markiereMatch(int matchid) {
		final int row = m_clTableSorter.getRow4Match(matchid);

		if (row > -1) {
			setRowSelectionInterval(row, row);
		} else {
			clearSelection();
		}
	}

	private void initModel(int iMatchType, MatchLocation matchLocation) {
		setOpaque(false);

		if (m_clTableModel == null) {
			m_clTableModel = UserColumnController.instance().getMatchesModel();
			m_clTableModel.setValues(DBManager.instance().getMatchesKurzInfo(HOVerwaltung.instance().getModel().getBasics().getTeamId(), iMatchType, matchLocation, false));
			m_clTableSorter = new TableSorter(m_clTableModel,m_clTableModel.getDisplayedColumns().length - 1, -1);

			final ToolTipHeader header = new ToolTipHeader(getColumnModel());
			header.setToolTipStrings(m_clTableModel.getTooltips());
			header.setToolTipText("");
			setTableHeader(header);

			setModel(m_clTableSorter);

			final TableColumnModel tableColumnModel = getColumnModel();

			for (int i = 0; i < m_clTableModel.getColumnCount(); i++) {
				tableColumnModel.getColumn(i).setIdentifier(i);
			}

			int[][] targetColumn = m_clTableModel.getColumnOrder();

			// Reihenfolge -> nach [][1] sortieren
			targetColumn = Helper.sortintArray(targetColumn, 1);

			if (targetColumn != null) {
				for (int[] ints : targetColumn) {
					this.moveColumn(
							getColumnModel().getColumnIndex(ints[0]),
							ints[1]);
				}
			}

			m_clTableSorter.addMouseListenerToHeaderInTable(this);
			m_clTableModel.setColumnsSize(getColumnModel());
		} else {
			// Reset Values of the matches table in matches module after selection change
			m_clTableModel.setValues(DBManager.instance().getMatchesKurzInfo(
					HOVerwaltung.instance().getModel().getBasics().getTeamId(), iMatchType, matchLocation,false));
			m_clTableSorter.reallocateIndexes();
		}

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setRowSelectionAllowed(true);

		m_clTableSorter.initsort();
	}
}
