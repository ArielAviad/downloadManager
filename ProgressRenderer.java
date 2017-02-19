import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ProgressRenderer extends JProgressBar implements TableCellRenderer{

    ProgressRenderer(int min, int max){
        super(min,max);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setValue((int)((Float) value).floatValue());
        return this;
    }
}
