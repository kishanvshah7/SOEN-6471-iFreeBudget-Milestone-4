package net.mjrz.fm.ui.dialogs;

import static net.mjrz.fm.utils.Messages.tr;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.mjrz.fm.Main;
import net.mjrz.fm.ui.utils.GuiUtilities;
import net.mjrz.fm.utils.Profiles;
import net.mjrz.fm.utils.ZProperties;

public class LangChangeDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private String user = null;
	private JComboBox langCb = null;
	private Map<String, Locale> localeMap;

	public LangChangeDialog(JFrame parent, String user) {
		super(parent, "Change language - " + user, true);
		super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.user = user;

		localeMap = new HashMap<String, Locale>();

		initialize();
		GuiUtilities.addWindowClosingActionMap(this);

		String homedir = Profiles.getInstance().getPathForProfile(user);
		ZProperties.loadUserProperties(homedir.toString());
		ZProperties.replaceRuntimeProperty("FMHOME", homedir.toString());
	}

	private void initialize() {
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());

		langCb = new JComboBox(getAvailableLocales());
		String lang = ZProperties.getProperty("LANGUAGE_LOCALE");
		System.out.println("Current locale = " + lang);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weightx = 0.3;
		cp.add(new JLabel(tr("Select language")), gbc1);

		gbc1.gridx = 1;
		gbc1.gridy = 0;
		gbc1.insets = new Insets(10, 10, 10, 10);
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weightx = 0.7;
		cp.add(langCb, gbc1);

		gbc1.gridx = 0;
		gbc1.gridy = 1;
		gbc1.gridwidth = 2;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.CENTER;
		gbc1.weightx = 0.7;
		cp.add(getButtonPanel(), gbc1);
	}

	private JPanel getButtonPanel() {
		JPanel ret = new JPanel();
		ret.setLayout(new BoxLayout(ret, BoxLayout.X_AXIS));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					changeLanguage();
					dispose();
				}
				catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LangChangeDialog.this.dispose();
			}
		});

		ret.add(Box.createHorizontalGlue());
		ret.add(ok);
		ret.add(Box.createHorizontalStrut(5));
		ret.add(cancelButton);

		ret.setBorder(BorderFactory.createTitledBorder(""));
		return ret;
	}

	private void changeLanguage() throws FileNotFoundException, IOException {
		String sel = (String) langCb.getSelectedItem();
		Locale l = localeMap.get(sel);
		String val = l.getLanguage() + "_" + l.getCountry();

		String file = ZProperties.getProperty("FMHOME") + Main.PATH_SEPARATOR
				+ "conf/localesettings.properties";

		Reader in = new BufferedReader(new FileReader(file));

		Properties props = new Properties();
		props.load(in);

		in.close();

		props.setProperty("LANGUAGE.LOCALE", val);

		BufferedWriter out = new BufferedWriter(new FileWriter(file));

		props.store(out, "System generated - Do not edit!!!");

		ZProperties.initialize();
	}

	private String[] getAvailableLocales() {
		List<String> ret = new ArrayList<String>();
		
		Locale l = new Locale("en", "US");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("ca", "ES");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("de", "DE");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("nl", "NL");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("fr", "FR");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("es", "ES");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("it", "IT");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		l = new Locale("pl", "PL");
		localeMap.put(l.getDisplayLanguage(), l);		
		ret.add(l.getDisplayLanguage());
		
		Collections.sort(ret);

		String arr[] = new String[ret.size()];
		return ret.toArray(arr);
	}
}
