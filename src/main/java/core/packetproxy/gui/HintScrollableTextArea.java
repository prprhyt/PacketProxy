/*
 * Copyright 2019 DeNA Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package packetproxy.gui;

import packetproxy.common.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class HintScrollableTextArea extends JScrollPane{
	private HintTextArea hintTextArea;
	public HintScrollableTextArea(HintTextArea hintTextArea){
		super(hintTextArea);
		this.hintTextArea = hintTextArea;
	}

	public void setText(String arg0) {
		hintTextArea.setText(arg0);
	}

	public String getText() {
		return hintTextArea.getText();
	}
}