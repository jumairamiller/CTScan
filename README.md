# CTScan
Given a 3D dataset of a CT Head Scan, I made use of JavaFX and Graphics concepts and techniques - including:

- image rendering
- nearest neighbour resize,
- bilinear interpolation resize
- Maximum intensity projection
- histogram equalization

to create an application which can be used to get a better view to analyse 3D volumes of medical data

### How to Run:

I used IntelliJ to develop this program so the steps needed for other IDEs may differ:

- JavaFx should be bundled into IntelliJ but if this is not the case please install [JavaFx](https://openjfx.io/openjfx-docs/).

To the run configuration please add:

```bash
--module-path /path/to/javafx-sdk-15.0.1/lib --add-modules javafx.controls,javafx.fxml
```
This should be under the section add to VM options under edit configurations.


![](https://github.com/jumairamiller/CTScan/blob/master/visuals/MRI%20Scan%201.png)
![](https://github.com/jumairamiller/CTScan/blob/master/visuals/Sliders%20and%20MIP.gif)
![](https://github.com/jumairamiller/CTScan/blob/master/visuals/bilnear%20interpolation.gif)
![](https://github.com/jumairamiller/CTScan/blob/master/visuals/nearest%20neighbour%20resize.gif)
