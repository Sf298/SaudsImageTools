# SaudsImageTools
A multi-threaded image library.

# Usage
Each image has a base object i.e. ```ImageRaster```. When making any modifications to an image, it is wrapped in a new layer that modifies the value when an attempt is made to access the pixel.

## Create an Image
```java
// Blank image
int width1 = 3, height1 = 4, channels1 = 1;
Image newImage = ImageRaster.create(width1, height1, channels1);

// Pre-populated array of values
int width2 = 3, height2 = 4, channels2 = 1;
byte[] values = new byte[]{ 1,2,3, 4,5,6, 7,8,9, 10,11,12 }
Image newImage = ImageRaster.create(width2, height2, channels2, values);

// From a BufferedImage
File file1 = new File("/path/to/file/image1.jpg");
BufferedImage bufferedImage = ImageIO.read(file1);
Image newImage = ImageRaster.create(bufferedImage);

// From a file
File file2 = new File("/path/to/file/image1.jpg");
Image newImage = ImageRaster.create(file2);
```

## Pixel math
```java

```