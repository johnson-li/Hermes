# Hermes
In a complex road situation, one car’s insight is blocked somehow and it needs to utilize the insight of the other car to help it make decisions. For example, in the above situation, Car B wants to turn left, but it is not clear about the road situation on the left (the side of Car A). Fortunately, Car C has a clear view of this area. So Car B will use the camera from Car C to determine if it is safe to turn left immediately. Since the video processing requires some computation resources, the job may be completed on edge computers or on the car according to different situations.

![System Architecture](docs/archi.png?raw=true "Title")


# System Overview
Input: the driver give the instruction of turning left

Output: the car shows on screen if it is safe to turn left


# Timeline
1. Cars register themselves to the edge cloud, and update their location (GPS data) period to keep alive.
1. Car B send a request to the edge cloud, saying that he want to know if it is safe to turn left.
2. Upon receiving the request, the edge cloud determine the related cars/computers that can help make the decision and generate some jobs for the cars/computers. The jobs are encapsulated into docker containers.
3. The cars/computers execute the jobs. Car C open the camera and stream the video to a cloud computer. The cloud computer process the video and generate some data which represents the road condition. Car B receives the road condition and make decisions.
4. When the decision is made, Car B knows if it is safe to turn left. It notifies the edge cloud that the process is finished. The edge cloud terminate the jobs for the cars/computers and the resources are released.

# Related Techniques
- Container based task management
- 5G based video streaming & RPC communication
- GPU accelerated video processing

# How to run
```
script/run-experiment.sh
```

To run the system, you need to have at lease 3 machines, with docker installed. The script deploys containers to the machines and starts the system.


