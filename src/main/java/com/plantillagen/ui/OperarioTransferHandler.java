package com.plantillagen.ui;

import com.plantillagen.model.Operario;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class OperarioTransferHandler extends TransferHandler {

    public static final DataFlavor OPERARIO_FLAVOR;

    static {
        try {
            OPERARIO_FLAVOR = new DataFlavor(
                DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=\"" + Operario.class.getName() + "\""
            );
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTable table = (JTable) c;
        int row = table.getSelectedRow();
        if (row < 0) return null;
        OperarioTableModel model = (OperarioTableModel) table.getModel();
        Operario op = model.getOperarioAt(row);
        if (op == null) return null;
        return new OperarioTransferable(op);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (action == MOVE) {
            JTable table = (JTable) source;
            int[] selectedRows = table.getSelectedRows();
            OperarioTableModel model = (OperarioTableModel) table.getModel();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                model.removeOperario(selectedRows[i]);
            }
        }
    }

    @Override
    public boolean canImport(TransferSupport support) {
        return support.isDataFlavorSupported(OPERARIO_FLAVOR);
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) return false;
        try {
            Operario operario = (Operario) support.getTransferable()
                .getTransferData(OPERARIO_FLAVOR);
            JTable table = (JTable) support.getComponent();
            OperarioTableModel model = (OperarioTableModel) table.getModel();
            if (model.contains(operario)) return false;

            int insertRow = table.getRowCount();
            if (support.isDrop()) {
                JTable.DropLocation dl =
                    (JTable.DropLocation) support.getDropLocation();
                int dropRow = dl.getRow();
                if (dropRow >= 0 && dropRow <= table.getRowCount()) {
                    insertRow = dropRow;
                }
            }
            model.insertOperarioAt(insertRow, operario);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static class OperarioTransferable implements Transferable {

        private final Operario operario;

        public OperarioTransferable(Operario operario) {
            this.operario = operario;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{OPERARIO_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return OPERARIO_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return operario;
        }
    }
}
