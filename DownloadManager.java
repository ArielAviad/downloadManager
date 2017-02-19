
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class DownloadManager extends JFrame implements Observer{

    private JTextField addTextField;
    private DownloadsTableModel tableModel;
    private JTable table;

    private JButton pauseButton, resumeButton;
    private JButton cancelButton, clearButton;

    private Download selectedDownload;

    private boolean clearing;

    public DownloadManager() {
        setTitle("Download Manger");
        setSize(640, 480);
        addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(e -> actionExit());

        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel addPanel = new JPanel();
        addTextField = new JTextField(30);
        addTextField.addActionListener(e -> actionAdd());
        JButton addButton = new JButton("New Download");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionAdd();
            }
        });        addPanel.add(addTextField);
        addPanel.add(addButton);

        tableModel = new DownloadsTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(
                e -> tableSelectionChanged()
        );

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        renderer.setStringPainted(true);
        table.setDefaultRenderer(JProgressBar.class, renderer);

        table.setRowHeight((int) renderer.getPreferredSize().getHeight());

        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setBorder(
                BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table),BorderLayout.CENTER);

        JPanel  buttonsPanel = new JPanel();
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> actionPause());
        pauseButton.setEnabled(false);
        buttonsPanel.add(pauseButton);

        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(e -> actionResume());
        resumeButton.setEnabled(false);
        buttonsPanel.add(resumeButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> actionCancel());
        cancelButton.setEnabled(false);
        buttonsPanel.add(cancelButton);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> actionClear());
        clearButton.setEnabled(false);
        buttonsPanel.add(clearButton);

        setLayout(new BorderLayout());
        add(addPanel,BorderLayout.NORTH);
        add(downloadsPanel,BorderLayout.CENTER);
        add(buttonsPanel,BorderLayout.SOUTH);
    }

    private void actionClear(){
        clearing = true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing = false;
        selectedDownload = null;
        updateButtons();
    }

    private void actionCancel() {
        selectedDownload.cancel();
        updateButtons();
    }

    private void actionResume() {
        selectedDownload.resume();
        updateButtons();
    }

    private void actionPause() {
        selectedDownload.pause();
        updateButtons();
    }

    // Called when table row selection changes.
    private void tableSelectionChanged() {
        if (selectedDownload != null)
            selectedDownload.deleteObserver(DownloadManager.this);
        if (!clearing && table.getSelectedRow() > -1){
            selectedDownload = tableModel.getDownload(table.getSelectedRow());
            selectedDownload.addObserver(DownloadManager.this);
            updateButtons();
        }
    }

    private void updateButtons() {
        if (selectedDownload != null) {
            int status = selectedDownload.getStatus();
            switch (status) {
                case Download.DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.PAUSED:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case Download.ERROR:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
                default: // COMPLETE or CANCELLED
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
            }
        } else {
            // No download is selected in table.
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);
        }
    }

    private void actionAdd() {
        if (addTextField.getText().equals(""))return;
        URL verifiedUrl = verifyUrl(addTextField.getText());
        if (verifiedUrl == null){
            JOptionPane.showMessageDialog(this,"Invalid Download URL",
                    "Error",JOptionPane.ERROR_MESSAGE);
        }
        else {
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText("");
        }
    }

    @Nullable
    private URL verifyUrl(String url) {
        if (!url.toLowerCase().startsWith("http://"))
            return null;
        URL verifyURL;
        try{
            verifyURL = new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }

        if (verifyURL.getFile().length() < 2)
            return null;

        return verifyURL;
    }

    @Contract(" -> fail")
    private void actionExit() {
        System.exit(0);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (selectedDownload != null && selectedDownload.equals(o))
            updateButtons();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DownloadManager manager = new DownloadManager();
                manager.setVisible(true);
            }
        });
    }

}