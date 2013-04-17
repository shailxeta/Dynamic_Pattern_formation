'''
This image server is capable of detecting four colors :
color 1 : yellow
color 2 : blue
color 3 : green
color 4 : dark pink

'''
import cv2.cv
import socket
global imghsv
global imgobst
global imager

#### hue values for four colors .... (change according to your enviornment.)
yellow_min = 20
yellow_max = 30
blue_min = 100
blue_max = 120
green_min = 55
green_max = 95
pink_min = 160
pink_max = 180

####

''' function to generate threshold image for four colors '''
def getthresholdedimg(im):
        global imghsv
        global yellow_min,yellow_max,blue_min,blue_max,green_min,green_max,pink_min,pink_max
        
        imghsv=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,3)
        cv2.cv.CvtColor(im,imghsv,cv2.cv.CV_BGR2HSV)  # Convert image from RGB to HSV

        img1=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,1)
        cv2.cv.InRangeS(imghsv,cv2.cv.Scalar(yellow_min,100,100),cv2.cv.Scalar(yellow_max,255,255),img1)        #yellow
        img2=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,1)
        cv2.cv.InRangeS(imghsv,cv2.cv.Scalar(blue_min,50,20),cv2.cv.Scalar(blue_max,255,255),img2)      #blue
        img3=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,1)
        cv2.cv.InRangeS(imghsv,cv2.cv.Scalar(green_min,50,20),cv2.cv.Scalar(green_max,255,255),img3)        #green
        img4=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,1)
        cv2.cv.InRangeS(imghsv,cv2.cv.Scalar(pink_min,50,50),cv2.cv.Scalar(pink_max,255,255),img4)      #pink

        imgthreshold=cv2.cv.CreateImage(cv2.cv.GetSize(im),8,1)

        # Adding all four images to a threshold image
        cv2.cv.Add(img1,img2,imgthreshold)
        cv2.cv.Add(imgthreshold,img3,imgthreshold)
        cv2.cv.Add(imgthreshold,img4,imgthreshold)               
              
        return imgthreshold

#################### MAIN ###################

print "waiting for connection..."
server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server_socket.bind(('', 6685))
hello,(addra,porta) = server_socket.recvfrom(512)

capture=cv2.cv.CaptureFromCAM(0)      #access default cam (you can change it)
frame = cv2.cv.QueryFrame(capture)
frame = cv2.cv.GetSubRect(frame,(100,22,390,390))        # subrect  (change according to given arena)
frame_size = cv2.cv.GetSize(frame)
grey_image = cv2.cv.CreateImage(cv2.cv.GetSize(frame), cv2.cv.IPL_DEPTH_8U, 1)
test=cv2.cv.CreateImage(cv2.cv.GetSize(frame),8,3)
img2=cv2.cv.CreateImage(cv2.cv.GetSize(frame),8,3)
cv2.cv.NamedWindow("Real",0)
cv2.cv.NamedWindow("Threshold",0)

A=[]
B=[]
C=[]
D=[]
x1 =0
y1 =0
x2 =0
y2 =0
x3 =0
y3 =0
x4 =0
y4 =0

#fo = open("foo.txt", "a")       # file for logging

while(1):
        color_image = cv2.cv.QueryFrame(capture)
        color_image = cv2.cv.GetSubRect(color_image,(100,22,390,390))        # subrect (change according to given arena)
        imdraw=cv2.cv.CreateImage(cv2.cv.GetSize(frame),8,3)
        cv2.cv.SetZero(imdraw)
        cv2.cv.Flip(color_image,color_image,1)
        cv2.cv.Smooth(color_image, color_image, cv2.cv.CV_GAUSSIAN, 3, 0)
        imgyellowthresh=getthresholdedimg(color_image)
        
        cv2.cv.Erode(imgyellowthresh,imgyellowthresh,None,3)
        cv2.cv.Dilate(imgyellowthresh,imgyellowthresh,None,10)
        img2=cv2.cv.CloneImage(imgyellowthresh)
        
        storage = cv2.cv.CreateMemStorage(0)
        contour = cv2.cv.FindContours(imgyellowthresh, storage, cv2.cv.CV_RETR_CCOMP, cv2.cv.CV_CHAIN_APPROX_SIMPLE)
        points = []

        ##### detecting colors coordinates #####

        while contour:
                # Draw bounding rectangles
                bound_rect = cv2.cv.BoundingRect(list(contour))
                contour = contour.h_next()
                pt1 = (bound_rect[0], bound_rect[1])
                pt2 = (bound_rect[0] + bound_rect[2], bound_rect[1] + bound_rect[3])
                points.append(pt1)
                points.append(pt2)
                cv2.cv.Rectangle(color_image, pt1, pt2, cv2.cv.CV_RGB(255,0,0), 2)
                #Calculating centroids
                centroidx=cv2.cv.Round((pt1[0]+pt2[0])/2)
                centroidy=cv2.cv.Round((pt1[1]+pt2[1])/2)

                if (yellow_min<cv2.cv.Get2D(imghsv,centroidy,centroidx)[0]<yellow_max):
                        A.append((centroidx,centroidy))
                        x1=centroidx
                        y1=centroidy
                elif (blue_min<cv2.cv.Get2D(imghsv,centroidy,centroidx)[0]<blue_max):
                        B.append((centroidx,centroidy))
                        x2=centroidx
                        y2=centroidy
                elif (green_min<cv2.cv.Get2D(imghsv,centroidy,centroidx)[0]<green_max):
                        C.append((centroidx,centroidy))
                        x3=centroidx
                        y3=centroidy
                elif (pink_min<cv2.cv.Get2D(imghsv,centroidy,centroidx)[0]<pink_max):
                        D.append((centroidx,centroidy))
                        x4=centroidx
                        y4=centroidy
                       
        try:
                cv2.cv.Circle(imdraw,A[1],5,(0,255,255))
                cv2.cv.Line(imdraw,A[0],A[1],(0,255,255),3,8,0)
                A.pop(0)
        except IndexError:
                print "Just wait for color 1"
        try:
                cv2.cv.Circle(imdraw,B[1],5,(255,0,0))
                cv2.cv.Line(imdraw,B[0],B[1],(255,0,0),3,8,0)
                B.pop(0)
        except IndexError:
                print "Just wait for color 2"
        try:
                cv2.cv.Circle(imdraw,C[1],5,(0,255,0))
                cv2.cv.Line(imdraw,C[0],C[1],(0,255,0),3,8,0)
                C.pop(0)
        except IndexError:
                print "Just wait for color 3"
        try:
                cv2.cv.Circle(imdraw,D[1],5,(0,0,255))
                cv2.cv.Line(imdraw,D[0],D[1],(0,0,255),3,8,0)
                D.pop(0)
        except IndexError:
                print "Just wait for color 4"

        server_socket.sendto('[['+ str(x1) +', '+  str(y1)+ '], ['+str(x2)+', '+ str(y2)+'], ['+str(x3)+', '+ str(y3)+'], ['+str(x4)+', '+ str(y4)+']]' ,(addra,porta))
       
        #print 'color 1 [', x1 , ',',y1, ']  color 2 [',x2,',',y2,']  color 3 [',x3,',',y3,']  color 4 [',x4,',',y4,']',obj
        #fo.write( 'color 1 ['+ str(x1) +','+  str(y1)+ ']  color 2 ['+str(x2)+','+ str(y2)+']  color 3 ['+str(x3)+','+ str(y3)+']  color 4 ['+str(x4)+','+ str(y4)+']  obstacle : ' + str(obj) +'\n')
       
        cv2.cv.Add(test,imdraw,test)
        cv2.cv.ShowImage("Real",color_image)
        cv2.cv.ShowImage("Threshold",img2)
        cv2.cv.ShowImage("Final",test)
        if cv2.cv.WaitKey(33)==1048603:
                cv2.cv.DestroyWindow("Real")
                cv2.cv.DestroyWindow("Threshold")
                fo.close()
                break
