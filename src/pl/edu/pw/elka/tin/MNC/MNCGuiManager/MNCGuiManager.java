package pl.edu.pw.elka.tin.MNC.MNCGuiManager;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;

/**
 * Menadzer do zarzadzania sterownikami
 * @author Paweł
 */
public class MNCGuiManager {
    /*
    public static void main(String[] args) {
        String text = "<b>NOWA</b><br/>";
        String all = "";
        JFrame frame = new JFrame("My App");
        frame.setSize(300,300);
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setText("<html>"+all+"</html>");
        JScrollPane scroll = new JScrollPane(pane);
        scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });
        frame.add(scroll);
        frame.setVisible(true);
        for(int i=0; i< 100; i++){
            all+=text;
            pane.setText("<html>"+all+"</html>");
            JScrollBar vertical = scroll.getVerticalScrollBar();
        }
    }
    */
    private JFrame frame;

    //private JTextArea textArea;

    private JScrollPane scrollPane;
    private JTextPane text_panel;
    private HTMLDocument doc;

    public MNCGuiManager() {
        //textArea = new JTextArea(20, 20);
        text_panel = new JTextPane();
        text_panel.setEditable(false);
        text_panel.setContentType("text/html");
        text_panel.setText("<html>" +
                "<head><style>body{margin:0; padding: 0; font-family: \"Courier New\", Courier, monospace; font-size: 11px;}" +
                ".log{padding: 2px 0 0 0; margin: 0 0 1px 0; height: 16px; background-color: #0099FF;}" +
                ".log-send{padding: 2px 0 0 0; margin: 0 0 1px 0; height: 16px; background-color: #00FF66;}</style></head>" +
                "<body><div class=\"log\">text1</div><div class=\"log\">text2</div></body></html>");
        doc = (HTMLDocument)text_panel.getStyledDocument();
        scrollPane = new JScrollPane(text_panel);

        frame = new JFrame("Test");
        frame.setPreferredSize(new Dimension(450, 300));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] arguments) {
        new MNCGuiManager().run();
    }

    public void run() {
        while (true) {
            Element e = doc.getElement(doc.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY);
            try {
                doc.insertBeforeEnd(e, "<div class=\"log-send\">"+Math.random()+"</div>");
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //textArea.append("" + Math.random() + "\n");
            if (shouldScroll()) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                    }

                });
            }
            try {
                Thread.sleep(500);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public boolean shouldScroll() {
        int minimumValue = scrollPane.getVerticalScrollBar().getValue() + scrollPane.getVerticalScrollBar().getVisibleAmount();
        int maximumValue = scrollPane.getVerticalScrollBar().getMaximum();
        if(text_panel.getSelectedText() != null)
            return false;
        return maximumValue == minimumValue;
    }
}
