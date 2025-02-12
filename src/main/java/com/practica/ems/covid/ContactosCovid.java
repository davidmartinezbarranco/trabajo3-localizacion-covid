package com.practica.ems.covid;


import com.practica.excecption.*;
import com.practica.genericas.*;
import com.practica.lista.ListaContactos;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactosCovid {
	private Poblacion poblacion;
	private Localizacion localizacion;
	private ListaContactos listaContactos;

	public ContactosCovid() {
		this.poblacion = new Poblacion();
		this.localizacion = new Localizacion();
		this.listaContactos = new ListaContactos();
	}

	public Poblacion getPoblacion() {
		return poblacion;
	}

	public void setPoblacion(Poblacion poblacion) {
		this.poblacion = poblacion;
	}

	public Localizacion getLocalizacion() {
		return localizacion;
	}

	public void setLocalizacion(Localizacion localizacion) {
		this.localizacion = localizacion;
	}
	
	

	public ListaContactos getListaContactos() {
		return listaContactos;
	}

	public void setListaContactos(ListaContactos listaContactos) {
		this.listaContactos = listaContactos;
	}

	public void loadData(String data, boolean reset) throws EmsInvalidTypeException, EmsInvalidNumberOfDataException,
			EmsDuplicatePersonException, EmsDuplicateLocationException {
		// borro información anterior.
		if (reset) {
			this.poblacion = new Poblacion();
			this.localizacion = new Localizacion();
			this.listaContactos = new ListaContactos();
		}
		String datas[] = dividirEntrada(data);
		for (String linea : datas) {
			String datos[] = this.dividirLineaData(linea);
			if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
				throw new EmsInvalidTypeException();
			}
			if (datos[0].equals("PERSONA")) {
				if (datos.length != Constantes.MAX_DATOS_PERSONA) {
					throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
				}
				this.poblacion.addPersona(this.crearPersona(datos));
			}
			if (datos[0].equals("LOCALIZACION")) {
				if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
					throw new EmsInvalidNumberOfDataException("El número de datos para LOCALIZACION es menor de 6");
				}
				PosicionPersona pp = this.crearPosicionPersona(datos);
				this.localizacion.addLocalizacion(pp);
				this.listaContactos.insertarNodoTemporal(pp);
			}
		}
	}

	@SuppressWarnings("resource")
	public void loadDataFile(String fichero, boolean reset) {
		FileReader fr = null;
		try {
			fr = abrirFichero(fichero, reset);
			BufferedReader br = new BufferedReader(fr);
			leerFichero(br);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cerrarFichero(fr);
		}
	}
	private void cerrarFichero(FileReader fr){
		try {
			if (null != fr) {
				fr.close();
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	private FileReader abrirFichero(String fichero, boolean reset){
		FileReader fr = null;
		try{
			File archivo = new File(fichero);
			fr = new FileReader(archivo);

			if (reset) {
				this.poblacion = new Poblacion();
				this.localizacion = new Localizacion();
				this.listaContactos = new ListaContactos();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return fr;
	}
	private void leerFichero(BufferedReader br){
		try{
			String datas[], data;
			while ((data = br.readLine()) != null) {
				datas = dividirEntrada(data.trim());
				for (String linea : datas) {
					String datos[] = this.dividirLineaData(linea);
					leerLinea(datos);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void leerLinea(String[] datos) {
		try{
			if (!datos[0].equals("PERSONA") && !datos[0].equals("LOCALIZACION")) {
				throw new EmsInvalidTypeException();
			}
			if (datos[0].equals("PERSONA")) {
				aniadirPersona(datos);
			}
			if (datos[0].equals("LOCALIZACION")) {
				aniadirLocalizacion(datos);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void aniadirLocalizacion(String[] datos) {
		try{
			if (datos.length != Constantes.MAX_DATOS_LOCALIZACION) {
				throw new EmsInvalidNumberOfDataException(
						"El número de datos para LOCALIZACION es menor de 6" );
			}
			PosicionPersona pp = this.crearPosicionPersona(datos);
			this.localizacion.addLocalizacion(pp);
			this.listaContactos.insertarNodoTemporal(pp);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void aniadirPersona(String[] datos) {
		try{
			if (datos.length != Constantes.MAX_DATOS_PERSONA) {
				throw new EmsInvalidNumberOfDataException("El número de datos para PERSONA es menor de 8");
			}
			this.poblacion.addPersona(this.crearPersona(datos));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int findPersona(String documento) throws EmsPersonNotFoundException {
		int pos;
		try {
			pos = this.poblacion.findPersona(documento);
			return pos;
		} catch (EmsPersonNotFoundException e) {
			throw new EmsPersonNotFoundException();
		}
	}

	public int findLocalizacion(String documento, String fecha, String hora) throws EmsLocalizationNotFoundException {

		int pos;
		try {
			pos = localizacion.findLocalizacion(documento, fecha, hora);
			return pos;
		} catch (EmsLocalizationNotFoundException e) {
			throw new EmsLocalizationNotFoundException();
		}
	}

	public List<PosicionPersona> localizacionPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0;
		List<PosicionPersona> lista = new ArrayList<PosicionPersona>();
		Iterator<PosicionPersona> it = this.localizacion.getLista().iterator();
		while (it.hasNext()) {
			PosicionPersona pp = it.next();
			if (pp.getDocumento().equals(documento)) {
				cont++;
				lista.add(pp);
			}
		}
		if (cont == 0)
			throw new EmsPersonNotFoundException();
		else
			return lista;
	}

	public boolean delPersona(String documento) throws EmsPersonNotFoundException {
		int cont = 0, pos = -1;
		Iterator<Persona> it = this.poblacion.getLista().iterator();
		while (it.hasNext()) {
			Persona persona = it.next();
			if (persona.getDocumento().equals(documento)) {
				pos = cont;
			}
			cont++;
		}
		if (pos == -1) {
			throw new EmsPersonNotFoundException();
		}
		this.poblacion.getLista().remove(pos);
		return false;
	}

	private String[] dividirEntrada(String input) {
		String cadenas[] = input.split("\\n");
		return cadenas;
	}

	private String[] dividirLineaData(String data) {
		String cadenas[] = data.split("\\;");
		return cadenas;
	}

	private Persona crearPersona(String[] data) {
		Persona persona = new Persona();
		for (int i = 1; i < Constantes.MAX_DATOS_PERSONA; i++) {
			String s = data[i];
			if(i == 1)
				persona.setDocumento(s);
			if(i == 2)
				persona.setNombre(s);
			if(i == 3)
				persona.setApellidos(s);
			if(i == 4)
				persona.setEmail(s);
			if(i == 5)
				persona.setDireccion(s);
			if(i == 6)
				persona.setCp(s);
			if(i == 7)
				persona.setFechaNacimiento(FechaHora.parsearFecha(s));
		}
		return persona;
	}

	private PosicionPersona crearPosicionPersona(String[] data) {
		PosicionPersona posicionPersona = new PosicionPersona();
		String fecha = null, hora;
		float latitud = 0, longitud;
		for (int i = 1; i < Constantes.MAX_DATOS_LOCALIZACION; i++) {
			String s = data[i];
			if(i == 1)
				posicionPersona.setDocumento(s);
			if(i == 2)
				fecha = data[i];
			if(i == 3) {
				hora = data[i];
				posicionPersona.setFechaPosicion(FechaHora.parsearFecha(fecha, hora));
			}
			if(i == 4)
				latitud = Float.parseFloat(s);
			if(i == 5) {
				longitud = Float.parseFloat(s);
				posicionPersona.setCoordenada(new Coordenada(latitud, longitud));
			}
		}
		return posicionPersona;
	}
}
