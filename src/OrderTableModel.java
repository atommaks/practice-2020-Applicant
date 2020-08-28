import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class OrderTableModel extends AbstractTableModel{
    private int columnCount = 7;
    private ArrayList<Object[]> data;

    public OrderTableModel(){
        data = new ArrayList<>();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
        return false;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex)[columnIndex];
    }

    @Override
    public String getColumnName(int columnIndex){
        switch (columnIndex){
            case 0: return "id";
            case 1: return "Дата создания";
            case 2: return "Пользователь";
            case 3: return "Тип заявки";
            case 4: return "Статус";
            case 5: return "Исполнитель";
            case 6: return "Axapta";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex){
        if (columnIndex == 6)
            return Boolean.class;
        return String.class;
    }

    @Override
    public void setValueAt(Object value, int i, int j){
        data.get(i)[j] = value;
        fireTableCellUpdated(i, j);
    }

    public void addRow(Object[] row){
        int n = getRowCount();
        data.add(row);
        fireTableRowsInserted(n, n + 1);
    }

    public void deleteRow(int rowIndex) {
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void repaint(){
        data.clear();
        fireTableDataChanged();
    }
}