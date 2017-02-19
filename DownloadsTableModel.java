import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class DownloadsTableModel extends AbstractTableModel implements Observer {

    static final String[] columnsNames = {
        "URL","Size","Progress","Status"
    };

    private static final Class[] columnClasses = {
            String.class,String.class, JProgressBar.class, String.class
    };

    private ArrayList<Download> downloadsList = new ArrayList<>();

    void addDownload(Download download){
        download.addObserver(this);
        downloadsList.add(download);
        fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
    }

    Download getDownload(int row){return downloadsList.get(row);}

    void clearDownload(int row){
        downloadsList.remove(row);
        fireTableRowsDeleted(row,row);
    }

    @Override
    public int getRowCount() {
        return downloadsList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsNames.length;
    }

    @Override
    public String getColumnName(int col){return col < 0  || col > columnsNames.length ? "" : columnsNames[col];}

    @Override
    public Class getColumnClass(int col){return col < 0 || col > columnClasses.length ? null : columnClasses[col];}


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Download download = downloadsList.get(rowIndex);
        switch (columnIndex){
            case 0://URL
                return download.getURL();
            case 1://Size
                int size = download.getSize();
                return (size < 0) ? "" : Integer.toString(size);
            case 2://Progress
                return new Float(download.getProgress());
            case 3://status
                return Download.STATUSES[download.getStatus()];
            default:return "";
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        int index = downloadsList.indexOf(o);
        fireTableRowsUpdated(index,index);
    }
}
