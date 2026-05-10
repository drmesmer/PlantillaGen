package com.plantillagen.db;

import com.plantillagen.model.LineaProduccion;
import com.plantillagen.model.Operario;
import com.plantillagen.model.PlantillaEntry;
import com.plantillagen.model.PlantillaHeader;

import java.util.List;

public class DaoTest {

    public static void main(String[] args) {
        try {
            OperarioDAO operarioDAO = new OperarioDAO();
            List<Operario> ops = operarioDAO.findAll();
            System.out.println("Operarios: " + ops.size());
            if (!ops.isEmpty()) {
                Operario first = ops.get(0);
                System.out.println("  Primero: [" + first.getId() + "] "
                    + first.getCodigo() + " - " + first.getNombre());
            }

            LineaDAO lineaDAO = new LineaDAO();
            List<LineaProduccion> lineas = lineaDAO.findAll();
            System.out.println("Lineas activas: " + lineas.size());
            lineas.forEach(l -> System.out.println("  " + l.getPosicion() + " -> " + l.getNombre()
                + " [" + l.getCategoria() + "] color=" + l.getColor()));

            PlantillaHeaderDAO headerDAO = new PlantillaHeaderDAO();
            PlantillaHeader h = new PlantillaHeader("Test 08052026_19_v1", "BORRADOR");
            int hId = headerDAO.save(h);
            System.out.println("Plantilla creada: id=" + hId);

            PlantillaDetalleDAO detalleDAO = new PlantillaDetalleDAO();
            PlantillaEntry e1 = new PlantillaEntry(
                lineas.get(0).getId(), ops.get(0).getId(), true, false, 0);
            e1.setTurnoId(1);
            PlantillaEntry e2 = new PlantillaEntry(
                lineas.get(0).getId(), ops.get(1).getId(), false, true, 1);
            e2.setTurnoId(1);
            detalleDAO.save(hId, e1);
            detalleDAO.save(hId, e2);

            List<PlantillaEntry> asignaciones =
                detalleDAO.findByPlantillaIdAndTurno(hId, 1);
            System.out.println("Asignaciones turno 1: " + asignaciones.size());
            asignaciones.forEach(a -> System.out.println("  " + a.getOperarioCodigo()
                + " " + a.getOperarioNombre()
                + " [Lider=" + a.isEsLider()
                + " Formacion=" + a.isTieneFormacion()
                + " Turno=" + a.getTurnoId() + "]"));

            detalleDAO.deleteByPlantillaId(hId);
            headerDAO.delete(hId);
            System.out.println("Test OK.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
